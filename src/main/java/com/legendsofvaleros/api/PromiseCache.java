package com.legendsofvaleros.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.util.Utilities;

import javax.annotation.Nonnull;
import java.util.Optional;
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

        Utilities.getInstance().getScheduler().executeInMyCircleTimer(() -> {
            // This is done so we get almost-live updates on GC'd listeners.
            this.cleanUp();
        }, 0L, 20L);
    }

    public V getIfPresent(K k) {
        return cache.getIfPresent(k);
    }

    public Optional<V> getAndWait(@Nonnull K k) {
        try {
            return Optional.ofNullable(this.get(k).get());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get '" + k + "'", e);
        }
    }

    public synchronized Promise<V> get(@Nonnull K k) {
        if(k == null)
            throw new NullPointerException("Cannot have a null key!");

        Promise<V> promise = new Promise<>();

        V cached = cache.getIfPresent(k);
        if(cached != null) {
            promise.resolve(cached);
        }else{
            if(!awaiting.containsKey(k)) {
                loader.loadValue(k).onSuccess(val -> {
                    put(k, val.orElse(null));
                }).onFailure(th -> {
                    for (Promise<V> ret : awaiting.removeAll(k))
                        ret.reject(th);
                });
            }

            awaiting.put(k, promise);
        }

        return promise;
    }

    public synchronized void put(K k, V v) {
        if(v != null)
            cache.put(k, v);

        for (Promise<V> ret : awaiting.removeAll(k))
            ret.resolve(v);
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

    public ConcurrentMap<K, V> asMap() {
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
