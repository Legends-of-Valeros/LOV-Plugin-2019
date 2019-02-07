package com.legendsofvaleros.api;

import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.concurrent.Executor;

public class Promise<T> {
    public interface Callback<T> {
        void run(Throwable th, T t);
    }

    /**
     * This is the default executor for the promise if a listener does
     * not define one themselves.
     */
    private final Executor executor;

    private final Set<Pair<Callback, Executor>> callbacks = new ConcurrentSet<>();

    private Object value;
    private Boolean result;

    public Promise() { this(MoreExecutors.directExecutor()); }
    public Promise(Executor executor) {
        this.executor = executor;
    }

    public boolean isDone() { return result != null; }
    public boolean wasSuccess() { return Boolean.TRUE.equals(result); }
    public boolean wasRejected() { return Boolean.FALSE.equals(result); }

    public void on(Promise.Callback<T> cb) { addListener(cb, null); }
    public void on(Promise.Callback<T> cb, Executor ex) { addListener(cb, ex); }

    public void addListener(Promise.Callback<T> cb) { addListener(cb, null); }
    public void addListener(Promise.Callback<T> cb, Executor ex) {
        callbacks.add(new ImmutablePair<>(cb, ex != null ? ex : executor));

        // If the promise is already fulfilled, fire the callback immediately
        if(isDone()) {
            if(wasSuccess())
                ex.execute(() -> cb.run(null, (T)value));
            else if(wasRejected())
                ex.execute(() -> cb.run((Throwable)value, null));
            else
                throw new IllegalStateException();
        }
    }

    public T get() throws Throwable {
        if(!isDone()) return null;
        if(this.value instanceof Throwable) throw (Throwable)this.value;
        return (T)this.value;
    }

    public void resolve(T t) {
        if(isDone()) return;

        this.value = t;
        this.result = true;

        callbacks.stream().forEach(cb -> {
            cb.getValue().execute(() -> cb.getKey().run(null, t));
        });
    }

    public void reject(Throwable th) {
        if(isDone()) return;

        this.value = th;
        this.result = false;

        callbacks.stream().forEach(cb -> {
            cb.getValue().execute(() -> cb.getKey().run(th, null));
        });
    }
}