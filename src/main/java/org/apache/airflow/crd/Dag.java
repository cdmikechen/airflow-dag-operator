package org.apache.airflow.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("airflow.apache.org")
@Version("v1")
public class Dag extends CustomResource<DagSpec, DagStatus> implements Namespaced {

}
