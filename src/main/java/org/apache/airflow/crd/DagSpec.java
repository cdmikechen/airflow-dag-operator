package org.apache.airflow.crd;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DagSpec {

    public enum Type {
        file,
        dag_file,
        dag_yaml
    }

    private Type type = Type.dag_file;

    private String path;

    @JsonProperty("file_name")
    private String fileName;

    private String context;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
