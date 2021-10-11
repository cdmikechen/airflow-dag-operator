package org.apache.airflow;

import javax.inject.Inject;

import org.apache.airflow.crd.Dag;
import org.apache.airflow.queue.DagQueue;
import org.apache.airflow.queue.DagTask;
import org.apache.airflow.type.ControlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class DagController implements ResourceController<Dag> {

    private static final Logger log = LoggerFactory.getLogger(DagController.class);

    @Inject
    DagQueue dagQueue;

    @Override
    public DeleteControl deleteResource(Dag dag, Context<Dag> context) {
        try {
            log.info("Queue delete dag task [{}] ...", dag.getMetadata().getName());
            dagQueue.push(new DagTask(dag.getMetadata().getName(), dag.getSpec(), ControlType.delete));
        } catch (InterruptedException e) {
            log.error("Error when queue task", e);
        }
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<Dag> createOrUpdateResource(Dag dag, Context<Dag> context) {
        try {
            log.info("Queue create/update dag task [{}] ...", dag.getMetadata().getName());
            dagQueue.push(new DagTask(dag.getMetadata().getName(), dag.getSpec(), ControlType.create));
        } catch (InterruptedException e) {
            log.error("Error when queue task", e);
        }
        return UpdateControl.noUpdate();
    }
}
