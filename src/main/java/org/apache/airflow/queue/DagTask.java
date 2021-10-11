package org.apache.airflow.queue;

import org.apache.airflow.crd.DagSpec;
import org.apache.airflow.type.ControlType;

public class DagTask {

    public DagTask(String name, DagSpec spec, ControlType type) {
        this.name = name;
        this.spec = spec;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    private DagSpec spec;

    private ControlType type;

    public DagSpec getSpec() {
        return spec;
    }

    public void setSpec(DagSpec spec) {
        this.spec = spec;
    }

    public ControlType getType() {
        return type;
    }

    public void setType(ControlType type) {
        this.type = type;
    }
}
