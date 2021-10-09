package org.apache.airflow;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.apache.airflow.crd.Dag;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class DagController implements ResourceController<Dag> {

    @Inject
    KubernetesClient client;

    @Override
    public DeleteControl deleteResource(Dag resource, Context<Dag> context) {
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<Dag> createOrUpdateResource(Dag resource, Context<Dag> context) {
        return null;
    }
}
