package org.apache.airflow.cache;

import java.util.Objects;

public class UnregisteredDagInstance {

    private String dagId;

    private String fullPath;

    private String namespace;

    private String name;

    private boolean paused;

    private Long createTime;

    private Long lastCheckTime;

    public UnregisteredDagInstance(String dagId, String fullPath, String namespace, String name, boolean paused) {
        this.dagId = dagId;
        this.fullPath = fullPath;
        this.namespace = namespace;
        this.name = name;
        this.paused = paused;
        this.createTime = System.currentTimeMillis();
    }

    public UnregisteredDagInstance(String dagId, String fullPath, boolean paused) {
        this.dagId = dagId;
        this.fullPath = fullPath;
        this.paused = paused;
        this.createTime = System.currentTimeMillis();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDagId() {
        return dagId;
    }

    public void setDagId(String dagId) {
        this.dagId = dagId;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public long checkScanInterval() {
        return lastCheckTime == null ? 0 : lastCheckTime - createTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnregisteredDagInstance that = (UnregisteredDagInstance) o;
        return paused == that.paused
                && Objects.equals(dagId, that.dagId)
                && Objects.equals(name, that.name)
                && Objects.equals(namespace, that.namespace)
                && Objects.equals(fullPath, that.fullPath)
                && Objects.equals(createTime, that.createTime)
                && Objects.equals(lastCheckTime, that.lastCheckTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dagId, fullPath, namespace, name, paused, createTime, lastCheckTime);
    }

    @Override
    public String toString() {
        return "UnregisteredDagInstance{" +
                "dagId='" + dagId + '\'' +
                ", fullPath='" + fullPath + '\'' +
                ", namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", paused=" + paused +
                ", createTime=" + createTime +
                ", lastCheckTime=" + lastCheckTime +
                '}';
    }
}
