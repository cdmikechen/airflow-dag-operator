package org.apache.airflow.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * There is a certain time interval from the generation of DAG files to the registration to airflow.
 * We put the unregistered DAG into the cache.
 */
public enum UnregisteredDagCache {

    CACHE;

    private final Map<String, UnregisteredDagInstance> cacheMap = new HashMap<>();

    public Set<String> getCacheKeys() {
        return cacheMap.keySet();
    }

    public boolean contains(String name) {
        return cacheMap.containsKey(name);
    }

    public UnregisteredDagInstance getInstance(String name) {
        return cacheMap.get(name);
    }

    public void cache(String name, UnregisteredDagInstance udi) {
        this.cacheMap.put(name, udi);
    }

    public void updateLastTime(String name, long time) {
        this.cacheMap.computeIfPresent(name, (key, value) -> {
            value.setLastCheckTime(time);
            return value;
        });
    }

    public void remove(String name, UnregisteredDagInstance udi) {
        if (udi != null) {
            // To prevent it from being added back in when it is deleted, add an object check here
            this.cacheMap.remove(name, udi);
        }
    }
}
