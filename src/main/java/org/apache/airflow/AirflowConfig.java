package org.apache.airflow;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "airflow.dag")
public interface AirflowConfig {

    String path();

    @WithName("max-thread")
    int maxThread();

    @WithName("default-user")
    String defaultUser();
}
