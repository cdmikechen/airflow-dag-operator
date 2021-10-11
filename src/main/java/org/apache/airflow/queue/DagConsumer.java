package org.apache.airflow.queue;

import org.apache.airflow.AirflowConfig;
import org.apache.airflow.crd.DagSpec;
import org.apache.airflow.type.ControlType;
import org.apache.airflow.type.DagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

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
                    createFile(task);
                }
            } catch (Exception e) {
                log.error("Get Dag Queue error！", e);
            }
        }
    }

    /**
     * Delete task file
     */
    private void deleteFile(DagTask task) {
        DagSpec spec = task.getSpec();
        String name = task.getName();
        DagType type = spec.getType();

        String filePath = "";
        switch (type) {
            case file:
                break;
            case dag_yaml:
            case dag_file:
            default:
                filePath = getFilePath(spec.getPath()) + getDagFile(name, spec.getFileName());
                break;
        }

        log.info("Delete dag {} in path {}", name, filePath);
        File deleteFile = new File(filePath);
        if (deleteFile.exists()) deleteFile.delete();
    }

    /**
     * Create task file
     */
    public void createFile(DagTask task) {
        DagSpec spec = task.getSpec();
        String name = task.getName();
        DagType type = spec.getType();

        switch (type) {
            case file:
                break;
            case dag_yaml:
                break;
            case dag_file:
            default:
                createDag(name, spec);
                break;
        }
    }

    /**
     * create dag file
     */
    private void createDag(String name, DagSpec spec) {
        // dag python file name
        String fileName = getDagFile(name, spec.getFileName());
        // dag file path
        String dagPath = getFilePath(spec.getPath());
        // dag content
        String content = spec.getContent();
        log.info("Create dag {} with file {} stored in {} and content {}",
                name, dagPath, fileName, content);

        try {
            // create folder if not exists
            File folderFile = new File(dagPath);
            if (!folderFile.exists()) {
                log.debug("Folder not exists, create folder {} ...", dagPath);
                folderFile.mkdirs();
            }

            // write file overwrite
            String filePath = dagPath + fileName;
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
        if (fileName == null) fileName = name;
        if (!fileName.startsWith(".py")) fileName = fileName + ".py";
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
        if (!folderPath.endsWith("/")) folderPath = folderPath + "/";
        // dag file path
        StringBuilder dagPath = new StringBuilder();
        dagPath.append(folderPath);

        if (path != null && !"".equals(path)) {
            dagPath.append(path.startsWith("/") ? path.substring(1) : path);
            if (!path.endsWith("/")) dagPath.append("/");
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
