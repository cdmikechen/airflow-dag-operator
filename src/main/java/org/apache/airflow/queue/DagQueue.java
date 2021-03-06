package org.apache.airflow.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.enterprise.context.ApplicationScoped;

import org.apache.airflow.AirflowConfig;
import org.apache.airflow.database.DatasourceService;
import org.apache.airflow.service.DagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DagQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DagQueue.class);

    private final BlockingQueue<DagTask> dagQueue;

    public DagQueue(AirflowConfig airflowConfig, DatasourceService datasourceService, DagService dagService) {
        this.dagQueue = new LinkedBlockingDeque<>();
        int maxThread = airflowConfig.maxThread();
        for (int i = 0; i < maxThread; i++) {
            LOGGER.info("Start DagConsumer-Thread-{}", i);
            new DagConsumer(dagService, airflowConfig, datasourceService, dagQueue, i).start();
        }
    }

    public void push(DagTask task) throws InterruptedException {
        this.dagQueue.put(task);
    }
}
