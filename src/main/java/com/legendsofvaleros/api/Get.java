package com.legendsofvaleros.api;

/**
 * Wrap a type from the API that needs to be referenced, but only in cases where that type may contain a circular reference.
 */
public class Get<T> {
    protected T val;

    public static <T> Get<T> of(T val) {
        return new Get<T>().set(val);
    }

    public static Get empty() { return new Get(); }

    public final Get<T> set(T val) {
        this.val = val;
        return this;
    }

    public final T get() {
        return val;
    }
}