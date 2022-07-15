package org.apache.airflow.database;

public class AirflowDag {

    private String dagId;

    private boolean paused;

    public AirflowDag() {
    }

    public AirflowDag(String dagId, boolean paused) {
        this.dagId = dagId;
        this.paused = paused;
    }

    public String getDagId() {
        return dagId;
    }

    public void setDagId(String dagId) {
        this.dagId = dagId;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public String toString() {
        return "AirflowDag{" +
                "dagId='" + dagId + '\'' +
                ", paused=" + paused +
                '}';
    }
}
