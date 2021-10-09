package org.apache.airflow;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.apache.airflow.crd.Dag;
import org.apache.airflow.crd.DagStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class DagController implements ResourceController<Dag> {

    private static final Logger log = LoggerFactory.getLogger(DagController.class);

    @Inject
    AirflowConfig airflowConfig;

    @Inject
    KubernetesClient client;

    @Override
    public DeleteControl deleteResource(Dag resource, Context<Dag> context) {
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<Dag> createOrUpdateResource(Dag dag, Context<Dag> context) {
        log.info("create dag with name {} and context {}", dag.getSpec().getFileName(), dag.getSpec().getContext());
        DagStatus status = new DagStatus();
        status.setState(DagStatus.State.CREATED);
        status.setError(false);
        dag.setStatus(status);
        return UpdateControl.updateStatusSubResource(dag);
    }
}
