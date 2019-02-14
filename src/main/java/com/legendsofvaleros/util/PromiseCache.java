package com.legendsofvaleros.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.Promise;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;

public class PromiseCache<K, V> {
    @FunctionalInterface
    public interface ILoader<K, V> {
        Promise<V> loadValue(K key);
    }

    private final Cache<K, V> cache;
    private final ILoader<K, V> loader;

    /**
     * Stores the futures waiting for a value to be loaded.
     */
    private Multimap<K, Promise<V>> awaiting = HashMultimap.create();

    public PromiseCache(Cache<K, V> cache, ILoader<K, V> loader) {
        this.cache = cache;
        this.loader = loader;
    }

    public V getIfPresent(K k) {
        return cache.getIfPresent(k);
    }

    public synchronized Promise<V> get(@Nonnull K k) {
        if(k == null) return null;

        Promise<V> promise = new Promise<>();

        V cached = cache.getIfPresent(k);
        if(cached != null)
            promise.resolve(cached);
        else{
            if(!awaiting.containsKey(k)) {
                loader.loadValue(k).onSuccess(val -> {
                    put(k, val.orElse(null));
                });
            }

            awaiting.put(k, promise);
        }

        return promise;
    }

    public synchronized void put(K k, V v) {
        if(v != null)
            cache.put(k, v);

        if(awaiting.containsKey(k)) {
            for (Promise<V> ret : awaiting.get(k))
                ret.resolve(v);
            awaiting.removeAll(k);
        }
    }

    public void invalidate(K k) {
        cache.invalidate(k);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public long size() {
        return cache.size();
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public ConcurrentMap asMap() {
        return cache.asMap();
    }

    public void cleanUp() {
        cache.cleanUp();
    }

    public void invalidateAll(Iterable iterable) {
        cache.invalidateAll(iterable);
    }

    public ImmutableMap getAllPresent(Iterable iterable) {
        return cache.getAllPresent(iterable);
    }
}
