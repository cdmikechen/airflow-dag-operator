package org.apache.airflow.cache;

import org.apache.airflow.type.DagType;

public class DagInstance {

    public DagInstance(String name, long version) {
        this.name = name;
        this.version = version;
    }

    private final long version;

    private final String name;

    private DagType type;

    private String path;

    private String fileName;

    private String content;

    public String getName() {
        return name;
    }

    public DagType getType() {
        return type;
    }

    public DagInstance setType(DagType type) {
        this.type = type;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public String getPath() {
        return path;
    }

    public DagInstance setPath(String path) {
        this.path = path;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public DagInstance setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getContent() {
        return content;
    }

    public DagInstance setContent(String content) {
        this.content = content;
        return this;
    }

    public String getFilePath() {
        return (path.endsWith("/") ? path : path.substring(0, path.length() - 1)) + fileName;
    }

    @Override
    public String toString() {
        return "DagInstance{" +
                "version=" + version +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", content='********'" +
                '}';
    }
}
