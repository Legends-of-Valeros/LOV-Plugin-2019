package com.legendsofvaleros.api;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Wrap a type from the API that needs to be referenced, but only in cases where that type may contain a circular reference.
 */
public class Ref<T> {
    private boolean isReady = false;

    protected T val;
    protected Promise<T> promise;

    public static <T> Ref<T> of(T val) {
        return new Ref<>(val);
    }

    public static <T> Ref<T> of(Promise<T> promise) {
        return new Ref<>(promise);
    }

    public static Ref empty() { return new Ref((Object)null); }

    private Ref(T val) {
        this.isReady = true;
        this.val = val;
    }

    private Ref(Promise<T> promise) {
        this.promise = promise;
        promise.on(() -> this.isReady = true);
    }

    public final void onReady(Consumer<Optional<T>> onReady) {
        promise.onSuccess(onReady);
    }

    public final Optional<T> getIfPresent() {
        return isReady ? Optional.ofNullable(this.val) : Optional.empty();
    }

    public final T get() {
        if(!isReady) {
            this.val = promise.get();

            this.isReady = true;
            this.promise = null;
        }

        return val;
    }
}