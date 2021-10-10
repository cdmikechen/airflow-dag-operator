package org.apache.airflow;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.apache.airflow.crd.Dag;
import org.apache.airflow.crd.DagSpec;
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
        DagSpec.Type type = dag.getSpec().getType();

        try {
            switch (type) {
                case dag_file:
                    DagStatus status = dag.getStatus();
                    String dagContext = getDagContext(dag);
                    String hash = MD5Util.crypt(dagContext);

                    if (status == null) {// status is null, need to create dag
                        createDag(dag, hash);
                        return UpdateControl.updateStatusSubResource(dag);
                    } else {
                        if (!hash.equals(status.getContextHash())) {
                            updateDag(dag, hash);
                            return UpdateControl.updateStatusSubResource(dag);
                        } else {
                            log.info("there is no change in dag {}, pass ...", dag.getCRDName());
                            return UpdateControl.noUpdate();
                        }
                    }
                default:
                    return UpdateControl.noUpdate();
            }
        } catch (Exception e) {
            log.error("error in create dag", e);
            DagStatus status = new DagStatus();
            status.setState(DagStatus.State.ERROR);
            status.setError(true);
            dag.setStatus(status);
            return UpdateControl.updateStatusSubResource(dag);
        }
    }

    private String getDagContext(Dag dag) {
        return dag.getSpec().getContext();
    }

    private void createDag(Dag dag, String hash) {
        log.info("create dag with name {} and context {}", dag.getSpec().getFileName(), dag.getSpec().getContext());
        DagStatus status = new DagStatus();
        status.setState(DagStatus.State.CREATED);
        status.setError(false);
        status.setMessage("created dag");
        status.setContextHash(hash);

        dag.setStatus(status);
    }

    private void updateDag(Dag dag, String hash) {
        log.info("update dag with name {} and context {}", dag.getSpec().getFileName(), dag.getSpec().getContext());
        DagStatus status = new DagStatus();
        status.setState(DagStatus.State.UPDATED);
        status.setError(false);
        status.setMessage("update dag");
        status.setContextHash(hash);

        dag.setStatus(status);
    }
}
