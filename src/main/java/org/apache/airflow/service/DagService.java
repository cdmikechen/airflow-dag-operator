package org.apache.airflow.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.*;
import org.apache.airflow.AirflowConfig;
import org.apache.airflow.crd.Dag;
import org.apache.airflow.crd.DagSpec;
import org.apache.airflow.queue.DagTask;
import org.apache.airflow.queue.FilePath;
import org.apache.airflow.service.pod.PodService;
import org.apache.airflow.type.DagType;
import org.apache.airflow.util.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;

@ApplicationScoped
public class DagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DagService.class);

    @Inject
    AirflowConfig airflowConfig;

    @Inject
    KubernetesClient client;

    @Inject
    PodService podService;

    /**
     * Delete file
     */
    public void deleteFilePath(String deletePath) throws IOException {
        Path deleteFile = Paths.get(deletePath);
        if (Files.exists(deleteFile))
            Files.delete(deleteFile);
    }

    /**
     * get dag file path and file name
     */
    public FilePath getFilePath(DagTask task) {
        return getFilePath(task.getName(), task.getSpec());
    }

    /**
     * get dag file path and file name
     */
    public FilePath getFilePath(String name, DagSpec spec) {
        DagType type = spec.getType();

        String filePath = getPath(spec.getPath());
        String fileName;
        switch (type) {
            case file:
                // file name
                fileName = spec.getFileName();
                if (fileName == null || "".equals(fileName))
                    throw new IllegalArgumentException("FileName can not be null!");
                break;
            case dag_yaml:
            case dag_file:
            default:
                // dag python file name
                fileName = getDagFile(name, spec.getDagName());
                break;
        }

        return new FilePath(filePath, fileName);
    }

    /**
     * Get dag file name. If fileName is null, use crd meta name
     *
     * @param name     dag crd meta name
     * @param fileName dag file name
     */
    public String getDagFile(String name, String fileName) {
        if (fileName == null || "".equals(fileName))
            fileName = name;
        if (!fileName.startsWith(".py"))
            fileName = fileName + ".py";
        return fileName;
    }

    /**
     * Get file path
     *
     * @param path dag path in dag resource
     * @return {dags_folder}/{custom_path}/
     */
    public String getPath(String path) {
        // dags folder path
        String folderPath = airflowConfig.path();
        if (!folderPath.endsWith("/"))
            folderPath = folderPath + "/";
        // dag file path
        StringBuilder dagPath = new StringBuilder();
        dagPath.append(folderPath);

        if (path != null && !"".equals(path)) {
            dagPath.append(path.startsWith("/") ? path.substring(1) : path);
            if (!path.endsWith("/"))
                dagPath.append("/");
        }
        return dagPath.toString();
    }

    /**
     * get dag or file content
     */
    public String getFileContent(DagTask task) {
        DagSpec spec = task.getSpec();
        DagType type = spec.getType();

        switch (type) {
            case dag_yaml:
                DagTemplate dagTemplate = new DagTemplate(task.getName(), task.getSpec().getDagYaml());
                return dagTemplate.createDagContent();
            case file:
            case dag_file:
            default:
                return spec.getContent();
        }
    }


    @ConfigProperty(name = "quarkus.operator-sdk.namespaces")
    Optional<String> namespaces;

    /**
     * @param path file path
     * @return true: ignore/false: not ignore
     */
    private boolean checkIgnorePath(Path path) {
        if (airflowConfig.ignorePath().isPresent()) {
            return Pattern.matches(airflowConfig.ignorePath().get(), path.toString());
        } else {
            return false;
        }
    }

    /**
     * Clear dags
     */
    public void clearDags() throws IOException {
        MixedOperation<Dag, KubernetesResourceList<Dag>, Resource<Dag>> dagClient = client.resources(Dag.class);
        List<Dag> dags = dagClient.inAnyNamespace().list().getItems();

        // namespace
        List<String> scanNs = new ArrayList<>();
        if (namespaces.isPresent() && StringUtils.notBlank(namespaces.get())) {
            scanNs.addAll(Arrays.asList(namespaces.get().split(",")));
            LOGGER.debug("Clear DAG in namespace {}", scanNs);
        }

        // transform dags to path
        List<Path> dagPaths = dags.stream()
                .filter(dag -> scanNs.isEmpty() || scanNs.contains(dag.getMetadata().getNamespace()))
                .map(dag -> getFilePath(dag.getMetadata().getName(), dag.getSpec()).getFilePath())
                .map(Paths::get)
                .collect(Collectors.toList());
        LOGGER.trace("Found {} DAG CRDs", dagPaths.size());

        // scan dags folder to compare
        String folderPath = airflowConfig.path();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            List<Path> deletePaths = paths.filter(Files::isRegularFile)
                    // skip ignore path and not contain in dag paths
                    .filter(path -> !checkIgnorePath(path) && !dagPaths.contains(path))
                    .collect(Collectors.toList());
            if (deletePaths.isEmpty()) {
                LOGGER.info("No invalid DAG found!");
            } else {
                for (Path deletePath : deletePaths) {
                    LOGGER.warn("Delete useless DAG in path {}", deletePath);
                    Files.delete(deletePath);
                }
            }
        }
    }

    /**
     * Pause or start DAG. Considering that the scheduler may lock the table,
     * we will call the scheduler's pod and use the airflow command to operate.
     */
    public void pauseDag(String dagId, boolean paused) {
        if (airflowConfig.schedulerResourceName().isPresent()) {
            podService.pauseDag(airflowConfig.schedulerResourceName().get(), airflowConfig.schedulerResourceType(),
                    dagId, paused);
        } else {
            LOGGER.warn("airflow scheduler resource name is empty, skip {} pause operate!", dagId);
        }
    }
}
