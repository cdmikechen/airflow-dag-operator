package org.apache.airflow.queue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import org.apache.airflow.AirflowConfig;
import org.apache.airflow.cache.DagCache;
import org.apache.airflow.cache.DagInstance;
import org.apache.airflow.crd.DagSpec;
import org.apache.airflow.type.ControlType;
import org.apache.airflow.type.DagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DagConsumer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(DagConsumer.class);

    private final AirflowConfig airflowConfig;

    private final BlockingQueue<DagTask> dagQueue;

    private final int thread;

    public DagConsumer(AirflowConfig airflowConfig, BlockingQueue<DagTask> dagQueue, int thread) {
        this.airflowConfig = airflowConfig;
        this.dagQueue = dagQueue;
        this.thread = thread;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DagTask task = dagQueue.take();
                if (task.getType() == ControlType.delete) {
                    deleteFile(task);
                } else {
                    String name = task.getName();
                    long version = task.getVersionNum();
                    if (!DagCache.INSTANCE.contains(name)) {
                        createFile(task);
                    } else {
                        long lastVersion = DagCache.INSTANCE.getInstance(name).getVersion();
                        // Check if this version is larger than this version
                        if (lastVersion <= version) {
                            updateFile(task);
                        } else {
                            log.warn("Can not create dag {}, current version {}, cache version {}",
                                    name, version, DagCache.INSTANCE.getInstance(name).getVersion());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Get Dag Queue error！", e);
            }
        }
    }

    /**
     * Delete task file
     */
    private void deleteFile(DagTask task) throws IOException {
        String name = task.getName();
        final StringBuilder filePath = new StringBuilder();

        if (DagCache.INSTANCE.contains(name)) {// if cached
            DagCache.INSTANCE.getInstance(name, task.getVersionNum())
                    .ifPresentOrElse(
                            dagInstance -> filePath.append(dagInstance.getFilePath()),
                            () -> log.warn("Can not delete dag {}, current version {}, cache version {}",
                                    name, task.getVersion(), DagCache.INSTANCE.getInstance(name).getVersion())
                    );
        } else {
            DagSpec spec = task.getSpec();
            DagType type = spec.getType();
            switch (type) {
                case file:
                    break;
                case dag_yaml:
                case dag_file:
                default:
                    filePath.append(getFilePath(spec.getPath())).append(getDagFile(name, spec.getDagName()));
                    break;
            }
        }

        if (filePath.length() > 0) {
            log.info("Delete dag {} in path {}", name, filePath);
            Path deleteFile = Paths.get(filePath.toString());
            if (Files.exists(deleteFile))
                Files.delete(deleteFile);
        }
    }

    /**
     * Create task file
     */
    public void createFile(DagTask task) {
        DagSpec spec = task.getSpec();
        String name = task.getName();
        DagType type = spec.getType();
        String filePath = getFilePath(spec.getPath());
        log.debug("Creating {} {} ...", type, name);

        DagInstance di = new DagInstance(name, task.getVersionNum())
                .setType(type)
                .setPath(filePath);
        switch (type) {
            case file:
                // file name
                String fileName = spec.getFileName();
                if (fileName == null || "".equals(fileName))
                    throw new IllegalArgumentException("FileName can not be null!");
                // create file
                createFile(filePath, fileName, spec.getContent());
                di.setFileName(fileName).setContent(spec.getContent());
                break;
            case dag_yaml:
                // dag python file name
                fileName = getDagFile(name, spec.getDagName());
                break;
            case dag_file:
            default:
                // dag python file name
                fileName = getDagFile(name, spec.getDagName());
                //  create file
                createFile(filePath, fileName, spec.getContent());
                di.setFileName(fileName).setContent(spec.getContent());
                break;
        }

        // save cache
        log.trace("Saving to cache {}", di);
        DagCache.INSTANCE.cache(name, di);
    }

    public void updateFile(DagTask task) {
        createFile(task);
    }

    /**
     * Create dag file
     *
     * @param path     file path
     * @param fileName file name
     * @param content  file content
     */
    private void createFile(String path, String fileName, String content) {
        log.info("Create {} stored in {} and content \n{}", fileName, path, content);
        try {
            // create folder if not exists
            Path folderFile = Paths.get(path);
            if (!Files.exists(folderFile)) {
                log.debug("Folder not exists, create folder {} ...", path);
                Files.createDirectories(folderFile);
            }

            // write file overwrite
            String filePath = path + fileName;
            bufferedWriter(filePath, content);
        } catch (IOException e) {
            log.error("Dag file error！", e);
        }
    }

    /**
     * Get dag file name. If fileName is null, use crd meta name
     *
     * @param name     dag crd meta name
     * @param fileName dag file name
     */
    private String getDagFile(String name, String fileName) {
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
    private String getFilePath(String path) {
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
     * write dag
     */
    private void bufferedWriter(String filepath, String content) throws IOException {
        log.debug("Saving dag file to {} ...", filepath);
        try (FileWriter fileWriter = new FileWriter(filepath);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(content);
        }
    }
}
