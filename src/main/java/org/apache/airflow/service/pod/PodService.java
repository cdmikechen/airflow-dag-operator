package org.apache.airflow.service.pod;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class PodService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodService.class);

    @Inject
    KubernetesClient client;

    /**
     * execute Pod Cmd
     */
    public void executePodCmd(Pod pod, String container, PodExecutor podExecutor) {
        PodStatus podStatus = pod.getStatus();
        String phase = podStatus.getPhase();
        if (!"Running".equalsIgnoreCase(phase)) {
            podExecutor.notRunHandle(pod);
        } else {
            // Enter the container exec and wait for confirmation
            CompletableFuture<String> data = new CompletableFuture<>();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ExecWatch watch = execCmd(pod, container, data, baos, "sh", "-c", podExecutor.command())
            ) {
                podExecutor.execute(pod, data);
            } catch (Exception e) {
                podExecutor.errorHandle(pod, e);
            }
        }
    }

    /**
     * execute cmd
     */
    public ExecWatch execCmd(Pod pod, String container, CompletableFuture<String> data,
                             ByteArrayOutputStream baos, String... command) {
        LOGGER.info("Exec command {} in pod {} and container {}", Arrays.asList(command),
                pod.getMetadata().getName(), container);
        return client.pods()
                .inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .inContainer(container)
                .writingOutput(baos)
                .writingError(baos)
                .usingListener(new PodListener(data, baos))
                .exec(command);
    }

    public static class PodListener implements ExecListener {

        private final CompletableFuture<String> data;
        private final ByteArrayOutputStream baos;

        public PodListener(CompletableFuture<String> data, ByteArrayOutputStream baos) {
            this.data = data;
            this.baos = baos;
        }

        @Override
        public void onFailure(Throwable t, Response failureResponse) {
            LOGGER.error("Exec command error：{}", t.getMessage());
            data.completeExceptionally(t);
        }

        @Override
        public void onClose(int code, String reason) {
            LOGGER.debug("Exit with: {} and with reason: {}", code, reason);
            data.complete(baos.toString());
        }
    }

    /**
     * Pause dag
     */
    public void pauseDag(String schedulerName, String schedulerType, String dagId, boolean paused) {
        LabelSelector selector = null;
        if ("deployment".equals(schedulerType)) {
            Deployment deployment = client.apps()
                    .deployments()
                    .withName(schedulerName)
                    .get();
            if (deployment != null) {
                selector = deployment.getSpec().getSelector();
            }
        } else if ("statefulset".equals(schedulerType)) {
            StatefulSet statefulset = client.apps()
                    .statefulSets()
                    .withName(schedulerName)
                    .get();
            if (statefulset != null) {
                selector = statefulset.getSpec().getSelector();
            }
        } else {
            LOGGER.warn("Not supported scheduler resource type {}, skip {} pause operate!",
                    schedulerType, dagId);
            return;
        }
        // find pod
        List<Pod> pods = client.pods()
                .withLabelSelector(selector)
                .list()
                .getItems();
        if (pods.isEmpty()) {
            LOGGER.warn("Can not find available pods with scheduler name {}, skip {} pause operate!",
                    schedulerType, dagId);
        } else {
            Optional<Pod> pod = pods.stream().filter(p -> "Running".equalsIgnoreCase(p.getStatus().getPhase())).findFirst();
            if (pod.isEmpty()) {
                LOGGER.warn("There is no running scheduler pod, skipping dag_id {} pause task!", dagId);
            } else {
                executePodCmd(pod.get(), "scheduler",
                        new PodExecutor() {
                            @Override
                            public void notRunHandle(Pod pod) {
                                LOGGER.error(String.format("Pod %s is not running!", pod.getMetadata().getName()));
                            }

                            @Override
                            public String command() {
                                if (paused) {
                                    return "airflow dags pause " + dagId;
                                } else {
                                    return "airflow dags unpause " + dagId;
                                }
                            }

                            @Override
                            public void execute(Pod pod, CompletableFuture<String> data) throws Exception {
                                //LOGGER.info("Exec Pod return：{}", data.get(5, TimeUnit.SECONDS));
                            }

                            @Override
                            public void errorHandle(Pod pod, Exception e) {
                                LOGGER.error(String.format("Exec pod %s error!", pod.getMetadata().getName()), e);
                            }
                        });
            }
        }
    }
}
