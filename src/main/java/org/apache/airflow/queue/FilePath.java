package org.apache.airflow.queue;

public class FilePath {

    String path;

    String fileName;

    public FilePath() {
    }

    public FilePath(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return (path.endsWith("/") ? path : path.substring(0, path.length() - 1)) + fileName;
    }
}
