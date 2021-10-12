package org.apache.airflow.crd;

import org.apache.airflow.type.DagType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DagSpec {

    private DagType type = DagType.dag_file;

    private String path;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("dag_name")
    private String dagName;

    private String content;

    @JsonProperty("dag_yaml")
    private DagYaml dagYaml;

    public DagType getType() {
        return type;
    }

    public void setType(DagType type) {
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

    public String getDagName() {
        return dagName;
    }

    public void setDagName(String dagName) {
        this.dagName = dagName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DagYaml getDagYaml() {
        return dagYaml;
    }

    public void setDagYaml(DagYaml dagYaml) {
        this.dagYaml = dagYaml;
    }
}
