package org.apache.airflow;

import javax.inject.Inject;

import io.javaoperatorsdk.operator.api.reconciler.*;
import org.apache.airflow.crd.Dag;
import org.apache.airflow.queue.DagQueue;
import org.apache.airflow.queue.DagTask;
import org.apache.airflow.type.ControlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ControllerConfiguration
public class DagReconciler implements Reconciler<Dag>, Cleaner<Dag> {

    private static final Logger log = LoggerFactory.getLogger(DagReconciler.class);

    @Inject
    DagQueue dagQueue;

    @Override
    public DeleteControl cleanup(Dag dag, Context context) {
        try {
            log.info("Queue delete dag task [{}/{}] ...",
                    dag.getMetadata().getNamespace(), dag.getMetadata().getName());
            dagQueue.push(new DagTask(dag.getMetadata().getNamespace(), dag.getMetadata().getName(),
                    dag.getMetadata().getResourceVersion(), dag.getSpec(), ControlType.delete));
        } catch (InterruptedException e) {
            log.error("Error when queue task", e);
        }
        return DeleteControl.defaultDelete();
    }

    @Override
    public UpdateControl<Dag> reconcile(Dag dag, Context context) {
        try {
            log.info("Queue create/update dag task [{}/{}] ...",
                    dag.getMetadata().getNamespace(), dag.getMetadata().getName());
            dagQueue.push(new DagTask(dag.getMetadata().getNamespace(), dag.getMetadata().getName(),
                    dag.getMetadata().getResourceVersion(), dag.getSpec(), ControlType.create));
        } catch (InterruptedException e) {
            log.error("Error when queue task", e);
        }
        return UpdateControl.noUpdate();
    }
}
