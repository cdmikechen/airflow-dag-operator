package org.apache.airflow.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Current node dag cache.
 * Save the existing DAG information into the cache to handle some logical operations during update and delete
 */
public enum DagCache {

    CACHE;

    private final Map<String, DagInstance> cacheMap = new HashMap<>();

    public Map<String, DagInstance> getCacheMap() {
        return cacheMap;
    }

    public boolean contains(String name) {
        return cacheMap.containsKey(name);
    }

    public DagInstance getInstance(String name) {
        return cacheMap.get(name);
    }

    /**
     * Get cache with version.
     * The purpose of this method is to prevent processing the old version data after the new version data
     */
    public Optional<DagInstance> getInstance(String name, long version) {
        DagInstance dagInstance = cacheMap.get(name);
        return dagInstance.getVersion() > version ? Optional.empty() : Optional.of(dagInstance);
    }

    public void cache(String name, DagInstance di) {
        this.cacheMap.put(name, di);
    }
}
