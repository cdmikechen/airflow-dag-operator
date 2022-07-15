package org.apache.airflow.service.pod;

import io.fabric8.kubernetes.api.model.Pod;

import java.util.concurrent.CompletableFuture;

public interface PodExecutor {

    /**
     * Processing method when there is no running pod
     */
    void notRunHandle(Pod pod);

    /**
     * Commands to be executed
     */
    String command();

    /**
     * Processing after execution returns results
     *
     * @param data We can use `data.get(5, TimeUnit.SECONDS)` to get the return result
     */
    void execute(Pod pod, CompletableFuture<String> data) throws Exception;

    /**
     * exception handling
     */
    void errorHandle(Pod pod, Exception e);
}
