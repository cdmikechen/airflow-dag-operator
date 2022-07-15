package org.apache.airflow.service.startup;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.airflow.service.DagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

/**
 * The purpose of this service is to remove useless DAG files based on existing CRDs.
 * This is useful when the service restarts or file conflict exceptions.
 */

@ApplicationScoped
public class ClearService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearService.class);

    @Inject
    DagService dagService;

    public void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Clearing useless DAGs ...");
        try {
            dagService.clearDags();
        } catch (IOException e) {
            LOGGER.error("Error when clearing useless DAGs!", e);
        }
    }
}
