package org.logviewer.core.cache;

public class NoCacheStrategy<K,V> implements CacheStrategy<K,V> {

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void setIfAbsent(K key, V value) {
        // nothing
    }
}
