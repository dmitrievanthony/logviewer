package org.logviewer.core.cache;

/**
 * Strategy pattern implementation for caching.
 * @param <K>
 * @param <V>
 */
public interface CacheStrategy<K,V> {

    V get(K key);

    void setIfAbsent(K key, V value);
}
