package org.apache.airflow.service.startup;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import org.apache.airflow.cache.UnregisteredDagCache;
import org.apache.airflow.cache.UnregisteredDagInstance;
import org.apache.airflow.crd.Dag;
import org.apache.airflow.database.AirflowDag;
import org.apache.airflow.database.DatasourceService;
import org.apache.airflow.service.DagService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * For the case that pause is supported, we will start an asynchronous scheduled task to scan the {@link UnregisteredDagCache}
 * to reconfirm whether the DAG has been registered with airflow
 */
@ApplicationScoped
public class UnregisteredDagSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnregisteredDagSchedulerService.class);

    @ConfigProperty(name = "airflow.dag.support-pause")
    Boolean supportPause;

    @ConfigProperty(name = "airflow.dag.scheduler-scan-interval")
    int scanInterval;// second

    @Inject
    DatasourceService datasourceService;

    @Inject
    DagService dagService;

    @Inject
    KubernetesClient client;

    private static final int DEFAULT_LOOP = 5;

    public void onStart(@Observes StartupEvent ev) {
        if (supportPause) {
            LOGGER.info("Need to start a scheduler thread to check unregistered dags ...");
            // we add one more loop
            long scanIntervalMillis = (scanInterval + DEFAULT_LOOP) * 1000L;
            new Thread(() -> {
                while (true) {
                    try {
                        // Check every 5 seconds
                        Thread.sleep(TimeUnit.SECONDS.toMillis(DEFAULT_LOOP));
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Scanning unregistered dags ...");
                        }
                        // get cache keys
                        Set<String> caches = Set.copyOf(UnregisteredDagCache.CACHE.getCacheKeys());
                        if (!caches.isEmpty()) {
                            for (String name : caches) {
                                try {
                                    check(name, scanIntervalMillis);
                                } catch (Exception e) {
                                    LOGGER.error("Error when check unregistered dag " + name, e);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted error when check unregistered dags!", e);
                    }
                }
            }).start();
        }
    }

    /**
     * Check by name
     */
    private void check(String name, long scanIntervalMillis) throws SQLException {
        UnregisteredDagInstance udi = UnregisteredDagCache.CACHE.getInstance(name);
        if (udi != null) {
            // Check whether the scan interval has been exceeded
            if (udi.getLastCheckTime() != null && udi.checkScanInterval() > scanIntervalMillis) {
                LOGGER.info("The scan interval has been exceeded, try to delete the cache {}", name);
                UnregisteredDagCache.CACHE.remove(name, udi);
            } else {
                LOGGER.debug("Check if dag {} has been registered (last check time: {})", udi.getDagId(),
                        new Timestamp(udi.getLastCheckTime() == null ? udi.getCreateTime() : udi.getLastCheckTime()));
                Optional<AirflowDag> dag = datasourceService.getAirflowDag(udi.getDagId());
                dag.ifPresentOrElse(d -> {
                    // get from dag custom resource, avoid changing paused when looping
                    MixedOperation<Dag, KubernetesResourceList<Dag>, Resource<Dag>> dagClient = client.resources(Dag.class);
                    Dag resource = dagClient.inNamespace(udi.getNamespace()).withName(udi.getName()).get();
                    if (resource == null) {
                        LOGGER.warn("Can not find dag resource {}/{}, maybe it has been deleted. Remove cache!", udi.getNamespace(), udi.getName());
                    } else {
                        if (!resource.getSpec().getPaused().equals(d.isPaused())) {
                            LOGGER.info("Start to set dag {} paused to {}", udi.getDagId(), resource.getSpec().getPaused());
                            dagService.pauseDag(udi.getDagId(), resource.getSpec().getPaused());
                        }
                    }
                    // remove cache
                    UnregisteredDagCache.CACHE.remove(name, udi);
                }, () -> {
                    try {
                        if (!datasourceService.importErrorDags(udi.getFullPath())) {
                            // If not found, update cache and put it back
                            UnregisteredDagCache.CACHE.updateLastTime(name, System.currentTimeMillis());
                        } else {
                            LOGGER.info("Dag {} has been imported error, remove it!", udi.getDagId());
                            UnregisteredDagCache.CACHE.remove(name, udi);
                        }
                    } catch (SQLException e) {
                        LOGGER.error("Error when check dag is imported error", e);
                    }
                });
            }
        }
    }

}
