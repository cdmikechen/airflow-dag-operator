# Airflow Dag Operator

Use K8s [CustomResourceDefinition](https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/) to replace Airflow Git Sync strategy.
The main idea of the project is to start a synchronization service with quarkus operator on each airflow pod to synchronize the DAG/files into the DAG folder.

This project has included the docker image packaging part (buildconfig or quarkus build) and the modified helm chart template based on the official airflow project (https://github.com/apache/airflow/tree/main/chart).

## Operation Strategy

* use `kubectl` to create rbac and `CustomResourceDefinition` (operator can automatically create resources)
* create some dag resource instances
* start quarkus operator
* operator can list dag resources or automatically perceive changes in resources, then create/update or delete dags in DAG folder.

## Dev Or Test

We can run our application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

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
