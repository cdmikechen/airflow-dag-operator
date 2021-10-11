package org.apache.airflow.queue;

import org.apache.airflow.AirflowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@ApplicationScoped
public class DagQueue {

    //private final AirflowConfig airflowConfig;

    private static final Logger log = LoggerFactory.getLogger(DagQueue.class);

    private final BlockingQueue<DagTask> dagQueue;

    public DagQueue(AirflowConfig airflowConfig) {
        //this.airflowConfig = airflowConfig;
        this.dagQueue = new LinkedBlockingDeque<>();
        int maxThread = airflowConfig.maxThread();
        for (int i = 0; i < maxThread; i++) {
            log.info("Start DagConsumer({})", i);
            new DagConsumer(airflowConfig, dagQueue, i).start();
        }
    }

    public void push(DagTask task) throws InterruptedException {
        this.dagQueue.put(task);
    }
}
