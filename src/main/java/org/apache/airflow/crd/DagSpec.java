package org.apache.airflow.crd;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.airflow.type.DagType;

public class DagSpec {

    private DagType type = DagType.dag_file;

    private String path;

    @JsonProperty("file_name")
    private String fileName;

    private String content;

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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
