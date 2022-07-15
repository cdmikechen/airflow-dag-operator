package org.apache.airflow;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "airflow.dag")
public interface AirflowConfig {

    String path();

    @WithName("max-thread")
    int maxThread();

    @WithName("default-user")
    String defaultUser();

    @WithName("ignore-path")
    Optional<String> ignorePath();

    @WithName("support-pause")
    @WithDefault("false")
    boolean supportPause();

    @WithName("scheduler-resource-name")
    Optional<String> schedulerResourceName();

    @WithName("scheduler-resource-type")
    String schedulerResourceType();

    @WithName("scheduler-scan-interval")
    @WithDefault("300")
    Integer schedulerScanInterval();
}
