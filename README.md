# Airflow Dag Operator

Use K8s [CustomResourceDefinition](https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/) 
to replace Airflow Git Sync strategy. The main idea of the project is to start a synchronization service 
with [Quarkus Operator](https://github.com/quarkiverse/quarkus-operator-sdk) 
on each airflow pod to synchronize the DAG/files into the DAG folder.

This project has included the docker image packaging part (buildconfig or quarkus build) 
and the modified helm chart template based on the official airflow project (https://github.com/apache/airflow/tree/main/chart).

## Operation Strategy

* use `kubectl` to create rbac and `CustomResourceDefinition` (operator can automatically create resources)
* create some dag resource instances
* start quarkus operator
* operator can list dag resources or automatically perceive changes in resources, then create/update or delete dags in DAG folder.

## CRD Schema

Resource description can be referred to [02-crd.yaml](example/02-crd.yaml) .
There are several important attributes in `CRD`, which are described here:

Parameter | Description | Default
--- | --- | ---
`type` | Type of CRD, it can be `dag_file`, `file` or `dag_yaml`. `dag_file` must be a DAG description file. `file` can be a python or other text format file.`dag_yaml` reference [dag-factory](https://github.com/ajbosco/dag-factory), but add some changes | `dag_file`
`path`| File path. If the file path is empty, it defaults to the root directory of `dags`, otherwise it is a subdirectory under `dags`  |
`file_name` | If `type` is `file`, we need a `file_name`. |
`dag_name` | If `type` is `dag_file` or `dag_yaml`, we need a `dag_name`. If `dag_name` don't have `.py` suffix, the operator will automatically append it. | crd name
`content`| If `type` is `dag_file` or `file`, It is the content of the file. |
`dag_yaml`| The described of DAG by yaml, For details, please refer to [dag-factory](https://github.com/ajbosco/dag-factory) |

## Dev Or Test

We can run our application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

An example has been in `/example` folder. In `/example`, it includes `RBAC`，`CRD`, some cases and `Deployment` for test.

## Quarkus Image Build

If we use OpenShift, we can use `BuildConfig` to build a native image. 
Otherwise, we can create a native executable using:
```shell script
./mvnw package -Pnative
# if use macOS, you should use -Dquarkus.native.container-build=true to build quarkus in docker with a linux environment
docker build -f src/main/docker/Dockerfile.native -t quarkus/airflow-dag-operator .
```

Or, if we don't have GraalVM installed, we can run the native executable build in a container using:
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native -t quarkus/airflow-dag-operator .
```

## Airflow Helm Chart

Helm dependency update to add postgresql chart and `lint`. We need helm3 to build.
```shell script
# dependency update
helm dep update

# lint
helm lint

# debug
helm install --dry-run --debug  -f values.yaml airflow -n airflow .
```

Deploy Chart
```shell script
# install
helm install -f values.yaml airflow -n airflow .

# upgrade
helm upgrade -f values.yaml airflow -n airflow .

# uninstall
helm uninstall airflow
```


