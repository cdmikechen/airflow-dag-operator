package org.apache.airflow.crd;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DagStatus {

    public enum State {
        CREATED,
        UPDATED,
        ALREADY_PRESENT,
        PROCESSING,
        ERROR,
        UNKNOWN
    }

    private State state = State.UNKNOWN;
    
    private boolean error;

    private String message;

    @JsonProperty("context_hash")
    private String contextHash;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContextHash() {
        return contextHash;
    }

    public void setContextHash(String contextHash) {
        this.contextHash = contextHash;
    }
}
