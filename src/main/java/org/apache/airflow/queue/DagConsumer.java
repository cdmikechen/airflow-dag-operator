package org.apache.airflow.queue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import org.apache.airflow.cache.DagCache;
import org.apache.airflow.cache.DagInstance;
import org.apache.airflow.crd.DagSpec;
import org.apache.airflow.service.DagService;
import org.apache.airflow.type.ControlType;
import org.apache.airflow.type.DagType;
import org.apache.airflow.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DagConsumer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(DagConsumer.class);

    private final DagService dagService;

    private final BlockingQueue<DagTask> dagQueue;

    private final int thread;

    public DagConsumer(DagService dagService, BlockingQueue<DagTask> dagQueue, int thread) {
        this.dagService = dagService;
        this.dagQueue = dagQueue;
        this.thread = thread;
    }

    public int getThread() {
        return thread;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("DagConsumer-Thread-" + getThread());
        while (true) {
            try {
                DagTask task = dagQueue.take();
                if (task.getType() == ControlType.delete) {
                    deleteFile(task);
                } else {
                    String name = task.getName();
                    long version = task.getVersionNum();
                    if (!DagCache.INSTANCE.contains(name)) {
                        log.debug("Creating {} {} ...", task.getSpec().getType(), name);
                        createFile(task);
                    } else {
                        DagInstance oldTask = DagCache.INSTANCE.getInstance(name);
                        long lastVersion = oldTask.getVersion();
                        // Check if this version is larger than this version
                        if (lastVersion <= version) {
                            log.debug("Updating {} {} ...", task.getSpec().getType(), name);
                            updateFile(task, oldTask);
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
                                    name, task.getVersion(), DagCache.INSTANCE.getInstance(name).getVersion()));
        } else {
            DagSpec spec = task.getSpec();
            DagType type = spec.getType();
            switch (type) {
                case file:
                    filePath.append(dagService.getPath(spec.getPath()))
                            .append(spec.getFileName());
                    break;
                case dag_yaml:
                case dag_file:
                default:
                    filePath.append(dagService.getPath(spec.getPath()))
                            .append(dagService.getDagFile(name, spec.getDagName()));
                    break;
            }
        }

        if (filePath.length() > 0) {
            log.info("Delete dag {} in path {}", name, filePath);
            dagService.deleteFilePath(filePath.toString());
        }
    }

    /**
     * Create task file
     */
    public void createFile(DagTask task) {
        DagSpec spec = task.getSpec();
        String name = task.getName();

        // get file path and name
        FilePath fp = dagService.getFilePath(task);
        String filePath = fp.getPath();
        String fileName = fp.getFileName();
        DagInstance di = new DagInstance(name, task.getVersionNum())
                .setType(spec.getType())
                .setPath(filePath)
                .setFileName(fileName);

        // get file content
        String content = dagService.getFileContent(task);
        di.setContent(content);

        // create file
        createFileContent(filePath, fileName, content);

        // save cache
        log.trace("Saving to cache {}", di);
        DagCache.INSTANCE.cache(name, di);
    }

    /**
     * Update task file
     */
    public void updateFile(DagTask task, DagInstance oldTask) throws IOException {
        DagSpec spec = task.getSpec();
        String name = task.getName();

        // get file path and name
        FilePath fp = dagService.getFilePath(task);
        String filePath = fp.getPath();
        String fileName = fp.getFileName();
        // get file content
        String newContent = dagService.getFileContent(task);

        DagInstance di = new DagInstance(name, task.getVersionNum())
                .setType(spec.getType())
                .setPath(filePath)
                .setFileName(fileName)
                .setContent(newContent);

        // Detect whether the data needs to be updated
        String oldPath = oldTask.getFilePath();
        String newPath = filePath + fileName;
        if (!oldPath.equals(newPath)) {
            // 1. path or file name had been changed, need to remove old file
            log.info("Need to delete old dag {} in path {}", name, oldPath);
            dagService.deleteFilePath(oldPath);

            log.info("Create new dag {} in path {}", name, oldPath);
            createFileContent(filePath, fileName, newContent);
        } else {
            Path newFile = Paths.get(newPath);
            if (!Files.exists(newFile)) {
                // 2. if file not exists, create it
                log.info("Can not find exists dag, create it!");
                createFileContent(filePath, fileName, newContent);
            } else {
                // 3. content had been changed, just use createFile method
                if (StringUtils.equals(newContent, oldTask.getContent())) {
                    log.debug("There is no difference between old and new contents!");
                } else {
                    log.info("The two contents are different, and the file needs to be rewritten!");
                    createFileContent(filePath, fileName, newContent);
                }
            }
        }

        // save cache
        log.trace("Saving to cache {}", di);
        DagCache.INSTANCE.cache(name, di);
    }

    /**
     * Create dag file
     *
     * @param path file path
     * @param fileName file name
     * @param content file content
     */
    private void createFileContent(String path, String fileName, String content) {
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
