package org.apache.airflow;

import io.smallrye.config.ConfigMapping;
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
}
