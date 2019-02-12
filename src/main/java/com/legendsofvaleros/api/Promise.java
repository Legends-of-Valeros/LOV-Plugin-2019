package com.legendsofvaleros.api;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Promise<T> {
    public interface Function<T> { T run() throws Throwable; }
    public interface Listener<T> { void run(Optional<Throwable> th, Optional<T> t); }

    enum State {PENDING, FIRED, RESOLVED, REJECTED }

    /**
     * Unsafe is used to park and unpark threads that directly call get()
     * functions. This function is less performance intensive than waiting
     * for a result to exist.
     */
    private static final Unsafe UNSAFE = getUnsafe();
    private static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe)f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This is the default executor for the promise if a listener does
     * not define one themselves.
     */
    private final Executor executor;

    /**
     * Threads that called get() and are waiting for the Promise to
     * be resolved.
     */
    private final Set<Thread> waiting = new HashSet<>();

    /**
     * Hold a list of callbacks and the executor used to fire them.
     *
     * I thought about using a HashSet here, but Maps give us the ability
     * to detect if a listener was bound a second time and makes firing
     * them a shorter operation.
     */
    private final Map<Listener, Executor> callbacks = new HashMap<>();

    /**
     * The value that was returned from the promise.
     */
    private Object value;

    /**
     * The current state of the promise.
     */
    private State state = State.PENDING;

    public Promise() { this(null); }

    /**
     * Executors are passed to then() and listener functions if new
     * executors are not supplied. Technically we don't need these,
     * it just allows some better syntactic sugar.
     */
    public Promise(Executor executor) {
        this.executor = executor != null ? executor : APIController.getInstance().getPool();
    }

    public boolean isDone() { return this.state != State.PENDING; }
    public boolean wasSuccess() { return this.state == State.RESOLVED; }
    public boolean wasRejected() { return this.state == State.REJECTED; }

    public Promise<T> addListener(Runnable cb) { return addListener((err, val) -> cb.run(), null); }
    public Promise<T> addListener(Listener<T> cb) { return addListener(cb, null); }
    public Promise<T> addListener(Listener<T> cb, Executor ex) {
        if(ex == null) ex = this.executor;

        callbacks.put(cb, ex);

        // If the promise is already fulfilled, fire the callback immediately
        if(isDone()) {
            if(wasSuccess())
                ex.execute(() -> cb.run(Optional.empty(), Optional.of((T)value)));
            else if(wasRejected())
                ex.execute(() -> cb.run(Optional.of((Throwable)value), Optional.empty()));
            else
                throw new IllegalStateException();
        }

        return this;
    }

    public Promise<T> on(Runnable cb) { return on((err, val) -> cb.run(), null); }
    public Promise<T> on(Listener<T> cb) { return on(cb, null); }
    public Promise<T> on(Listener<T> cb, Executor ex) { return addListener(cb, ex); }

    public Promise<T> onSuccess(Runnable cb) { return onSuccess(val -> cb.run(), null); }
    public Promise<T> onSuccess(Consumer<Optional<T>> cb) { return onSuccess(cb, null); }
    public Promise<T> onSuccess(Consumer<Optional<T>> cb, Executor ex) {
        return addListener((err, val) -> {
            if(err != null) return;
            cb.accept(val);
        }, ex);
    }

    public Promise<T> onFailure(Runnable cb) { return onFailure((err) -> cb.run(), null); }
    public Promise<T> onFailure(Consumer<Throwable> cb) { return onFailure(cb, null); }
    public Promise<T> onFailure(Consumer<Throwable> cb, Executor ex) {
        return addListener((err, val) -> {
            if(val != null) return;
            cb.accept(err.get());
        }, ex);
    }

    public <N> Promise<N> then(Function<N> func) { return then(func, this.executor); }
    public <N> Promise<N> then(Function<N> func, Executor ex) {
        Promise<N> promise = new Promise<>(ex);

        // Fire the next promise on success.
        onSuccess(val -> promise.fire(func), ex);

        return promise;
    }

    public <N> Promise<N> next(Function<Promise<N>> func) { return next(func, this.executor); }
    public <N> Promise<N> next(Function<Promise<N>> func, Executor ex) {
        Promise<N> promise = new Promise<>(ex);

        // Fire the next promise on success.
        onSuccess(__ -> {
            // When the promise returns, propagate it up to the proxy promise.
            try {
                func.run().addListener((err, val) -> {
                    if(err.isPresent()) {
                        promise.reject(err.get());
                        return;
                    }

                    promise.resolve(val.orElse(null));
                });
            } catch (Throwable th) {
                 promise.reject(th);
            }
        }, ex);

        return promise;
    }

    public T get() throws Throwable {
        return get(null, 0L);
    }

    public T get(TimeUnit unit, long duration) throws Throwable {
        if(!isDone()) {
            waiting.add(Thread.currentThread());

            if(unit != null)
                UNSAFE.park(true, unit.toSeconds(duration));
            else
                UNSAFE.park(false, 0L);

            waiting.remove(Thread.currentThread());

            if(Thread.interrupted())
                throw new InterruptedException();
        }

        if(this.value instanceof Throwable) throw (Throwable)this.value;
        return (T)this.value;
    }

    public void resolve(T t) {
        if(isDone()) return;

        this.value = t;
        this.state = State.RESOLVED;

        callbacks.forEach((k, v) -> v.execute(() -> k.run(Optional.empty(), Optional.ofNullable(t))));
        new HashSet<>(waiting).forEach(UNSAFE::unpark);
    }

    public void reject(Throwable th) {
        if(isDone()) return;

        // Always show rejected promises in the console
        new Exception("Promise rejected!", th).printStackTrace();

        this.value = th;
        this.state = State.REJECTED;

        callbacks.forEach((k, v) -> v.execute(() -> k.run(Optional.of(th), null)));
        new HashSet<>(waiting).forEach(UNSAFE::unpark);
    }


    /**
     * This is a helper function that fires a function and uses
     * the Promise object as a wrapper for its result. Promises
     * don't need knowledge of the function that called them,
     * and it gives us more flexibility if they don't.
     */
    private Promise<T> fire(Function<T> func) {
        if(this.state != State.PENDING)
            throw new IllegalStateException("Promise has already been made! You cannot fire it again!");

        this.state = State.PENDING;

        if(func != null) {
            this.executor.execute(() -> {
                try {
                    this.resolve(func.run());
                } catch (Throwable th) {
                    this.reject(th);
                }
            });
        }

        return this;
    }

    public static <T> Promise<T> make(Function<T> func) {
        return make(func, null);
    }

    public static <T> Promise<T> make(Function<T> func, Executor exec) {
        return new Promise<T>(exec).fire(func);
    }

    public static Promise<Object[]> collect(Promise... promises) {
        return collect(null, promises);
    }

    public static Promise<Object[]> collect(Executor exec, Promise... promises) {
        Promise<Object[]> promise = new Promise<>(exec);

        AtomicBoolean fail = new AtomicBoolean(false);

        Optional<Object>[] successes = new Optional[promises.length];
        Throwable[] failures = new Throwable[promises.length];

        AtomicInteger i = new AtomicInteger(promises.length);
        Runnable onFinish = () -> {
            if(i.decrementAndGet() == 0) {
                if(fail.get()) {
                    promise.reject(new RuntimeException("Some promises were rejected!"));
                    return;
                }

                promise.resolve(successes);
            }
        };

        for(int j = 0; j < promises.length; j++) {
            final int k = j;
            ((Promise<Object>)promises[j]).onSuccess(val -> {
                successes[k] = val;
                onFinish.run();
            });
        }

        for(int j = 0; j < promises.length; j++) {
            final int k = j;
            ((Promise<Object>)promises[j]).onFailure(err -> {
                failures[k] = err;

                fail.set(true);
                onFinish.run();
            });
        }

        return promise;
    }
}