package org.apache.airflow;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "airflow.dag")
public interface AirflowConfig {

    String path();
}
