package com.legendsofvaleros.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class FutureCache<K, V> {
    @FunctionalInterface
    public interface ILoader<K, V> {
        void loadValue(K key, SettableFuture<V> ret);
    }

    private final Cache<K, V> cache;
    private final ILoader<K, V> loader;

    /**
     * Stores the futures waiting for a value to be loaded.
     */
    private Multimap<K, SettableFuture<V>> awaiting = HashMultimap.create();

    public FutureCache(Cache<K, V> cache, ILoader<K, V> loader) {
        this.cache = cache;
        this.loader = loader;
    }

    public V getIfPresent(K k) {
        return cache.getIfPresent(k);
    }

    @Nullable
    public synchronized ListenableFuture<V> get(K k) {
        if(k == null) throw new NullPointerException("Key cannot be null!");

        SettableFuture<V> ret = SettableFuture.create();

        V cached = cache.getIfPresent(k);
        if(cached != null)
            ret.set(cached);
        else{
            if(!awaiting.containsKey(k)) {
                SettableFuture<V> future = SettableFuture.create();

                loader.loadValue(k, future);

                future.addListener(() -> {
                    try {
                        put(k, future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }, Utilities.getInstance().getScheduler()::async);
            }

            awaiting.put(k, ret);
        }

        return ret;
    }

    public synchronized void put(K k, V v) {
        if(v != null)
            cache.put(k, v);

        if(awaiting.containsKey(k)) {
            for (SettableFuture<V> ret : awaiting.get(k))
                ret.set(v);
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
