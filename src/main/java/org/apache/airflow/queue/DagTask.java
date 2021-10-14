package org.apache.airflow.queue;

import org.apache.airflow.crd.DagSpec;
import org.apache.airflow.type.ControlType;

public class DagTask {

    public DagTask(String namespace, String name, String version, DagSpec spec, ControlType type) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
        this.spec = spec;
        this.type = type;
    }

    public final String namespace;

    public final String version;

    public String getName() {
        return name;
    }

    private final String name;

    private DagSpec spec;

    private ControlType type;

    public String getVersion() {
        return version;
    }

    public long getVersionNum() {
        return Long.parseLong(version);
    }

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
