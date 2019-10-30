package com.legendsofvaleros.api;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.util.MessageUtil;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Promise<R> {
    public interface Function<R, V> {
        R run(@Nonnull Optional<V> val) throws Exception;
    }

    public interface FunctionAlone<R> {
        R run() throws Exception;
    }

    public interface Listener<R> {
        void run(@Nonnull Optional<Exception> th, @Nonnull Optional<R> t) throws Exception;
    }

    enum State { PENDING, RESOLVED, REJECTED }

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
            return (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private final String trace;

    /**
     * This is the default executor for the promise if a listener does
     * not define one themselves.
     */
    private final Executor executor;

    private final Object lock = new Object();

    /**
     * Threads that called get() and are waiting for the Promise to
     * be resolved.
     */
    // private final Set<Thread> waiting = new HashSet<>();

    /**
     * Hold a list of callbacks and the executor used to fire them.
     * <p>
     * I thought about using a HashSet here, but Maps give us the ability
     * to detect if a listener was bound a second time and makes firing
     * them a shorter operation.
     */
    private final Map<Listener, Executor> callbacks = new LinkedHashMap<>();

    /**
     * The value that was returned from the promise.
     */
    private Object value;

    /**
     * The current state of the promise.
     */
    private State state = State.PENDING;

    public Promise() {
        this(null);
    }

    /**
     * Executors are passed to then() and listener functions if new
     * executors are not supplied. Technically we don't need these,
     * it just allows some better syntactic sugar.
     */
    public Promise(Executor executor) {
        this.trace = MessageUtil.getStackTrace(new Throwable("Created Promise"));

        this.executor = executor != null ? executor : APIController.getInstance().getPool();
    }

    public boolean isDone() {
        return this.state != State.PENDING;
    }

    public boolean wasSuccess() {
        return this.state == State.RESOLVED;
    }

    public boolean wasRejected() {
        return this.state == State.REJECTED;
    }

    public Promise<R> addListener(Runnable cb) {
        return addListener((err, val) -> cb.run(), null);
    }

    public Promise<R> addListener(Listener<R> cb) {
        return addListener(cb, null);
    }

    public Promise<R> addListener(Listener<R> cb, Executor ex) {
        if (ex == null) ex = this.executor;

        callbacks.put(cb, ex);

        // If the promise is already fulfilled, fire the callback immediately
        if (isDone()) {
            if (wasSuccess())
                ex.execute(() -> {
                    try {
                        cb.run(Optional.empty(), Optional.ofNullable((R) value));
                    } catch (Exception th) {
                        th.printStackTrace();
                    }
                });
            else if (wasRejected())
                ex.execute(() -> {
                    try {
                        cb.run(Optional.of((Exception) value), Optional.empty());
                    } catch (Exception th) {
                        th.printStackTrace();
                    }
                });
            else
                throw new IllegalStateException();
        }

        return this;
    }

    public Promise<R> on(Promise<R> promise) {
        return on((err, val) -> {
            if (err.isPresent()) {
                promise.reject(err.get());
                return;
            }

            promise.resolve(val.orElse(null));
        }, null);
    }

    public Promise<R> on(Runnable cb) {
        return on((err, val) -> cb.run(), null);
    }

    public Promise<R> on(Listener<R> cb) {
        return on(cb, null);
    }

    public Promise<R> on(Listener<R> cb, Executor ex) {
        return addListener(cb, ex);
    }

    public Promise<R> onSuccess(Runnable cb) {
        return onSuccess(val -> cb.run(), null);
    }

    public Promise<R> onSuccess(Consumer<Optional<R>> cb) {
        return onSuccess(cb, null);
    }

    public Promise<R> onSuccess(Consumer<Optional<R>> cb, Executor ex) {
        return addListener((err, val) -> {
            if (err.isPresent()) return;
            cb.accept(val);
        }, ex);
    }

    public Promise<R> onFailure(Runnable cb) {
        return onFailure((err) -> cb.run(), null);
    }

    public Promise<R> onFailure(Consumer<Exception> cb) {
        return onFailure(cb, null);
    }

    public Promise<R> onFailure(Consumer<Exception> cb, Executor ex) {
        return addListener((err, val) -> {
            if (val.isPresent()) {
                return;
            }
            if (!err.isPresent()) {
                return;
            }
            cb.accept(err.get());
        }, ex);
    }

    public Promise<R> sleep(long ticks) {
        Promise<R> promise = new Promise<>(this.executor);

        onSuccess(val -> {
            APIController.getInstance().getScheduler().executeInMyCircleLater(() ->
                    promise.resolve(val.orElse(null)), ticks);
        });

        onFailure(promise::reject);

        return promise;
    }

    public <V> Promise<V> then(FunctionAlone<V> func) {
        return then(func, this.executor);
    }

    public <V> Promise<V> then(FunctionAlone<V> func, Executor exec) {
        return then(func, exec);
    }

    public <V> Promise<V> then(Function<V, R> func) {
        return then(func, this.executor);
    }

    public <V> Promise<V> then(Function<V, R> func, Executor ex) {
        Promise<V> promise = new Promise<>(ex);

        // Fire the next promise on success.
        onSuccess(val -> promise.fire(func, val.orElse(null)), ex);

        return promise;
    }

    public <V> Promise<V> next(FunctionAlone<Promise<V>> func) {
        return next(v -> func.run(), this.executor);
    }

    public <V> Promise<V> next(FunctionAlone<Promise<V>> func, Executor exec) {
        return next(v -> func.run(), exec);
    }

    public <V> Promise<V> next(Function<Promise<V>, R> func) {
        return next(func, this.executor);
    }

    public <V> Promise<V> next(Function<Promise<V>, R> func, Executor ex) {
        Promise<V> promise = new Promise<>(ex);

        // Fire the next promise on success.
        onSuccess(val -> {
            // When the promise returns, propagate it up to the proxy promise.
            try {
                func.run(val).on((err, val2) -> {
                    if (err.isPresent()) {
                        promise.reject(err.get());
                        return;
                    }

                    promise.resolve(val2.orElse(null));
                });
            } catch (Exception th) {
                promise.reject(th);
            }
        }, ex);

        return promise;
    }

    public R get() {
        synchronized (lock) {
            while (!isDone()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /*if (!isDone()) {
            waiting.add(Thread.currentThread());

            if (unit != null)
                UNSAFE.park(true, unit.toSeconds(duration));
            else
                UNSAFE.park(false, 0L);

            waiting.remove(Thread.currentThread());

            if (Thread.interrupted())
                throw new RuntimeException(new InterruptedException());
        }*/

        if (this.value instanceof Exception) {
            throw new RuntimeException((Exception)this.value);
        }

        return (R) this.value;
    }

    public void resolve(R t) {
        if (isDone()) throw new IllegalStateException("Promise already resolved!");

        this.value = t;
        this.state = State.RESOLVED;

        this.execute(callbacks.entrySet().iterator(), Optional.empty(), Optional.ofNullable(t));

        synchronized(lock) {
            lock.notify();
        }

        // new HashSet<>(waiting).forEach(UNSAFE::unpark);
    }

    public void reject(Exception th) {
        if (isDone()) {
            throw new IllegalStateException("Promise already resolved!");
        }

        // Always show rejected promises in the console
        LegendsOfValeros.getInstance().getLogger().warning("------------PROMISE REJECTED------------");
        int i = -1;
        for (String line : trace.split("\n")) {
            if (i == -1) {
                // Ignore leading non-LOV packages
                if (!line.contains("com.legendsofvaleros") || line.contains("com.legendsofvaleros.api")) {
                    continue;
                }
            }

            i++;

            LegendsOfValeros.getInstance().getLogger().warning(line);

            // Don't print too many lines. After an amount, it's just spam.
            if (i >= 5 && !line.contains("legendsofvaleros")) {
                break;
            }
        }

        LegendsOfValeros.getInstance().getLogger().warning("-----------------TRACE------------------");

        // Print the actual reason for rejection
        String[] lines = MessageUtil.getStackTrace(th).split("\n");
        for(int j = 0; j < Math.min(lines.length, 20); j++)
            LegendsOfValeros.getInstance().getLogger().warning(lines[j]);

        LegendsOfValeros.getInstance().getLogger().warning("------------------END-------------------");

        this.value = th;
        this.state = State.REJECTED;

        this.execute(callbacks.entrySet().iterator(), Optional.of(th), Optional.empty());

        synchronized(lock) {
            lock.notify();
        }

        // new HashSet<>(waiting).forEach(UNSAFE::unpark);
    }

    private void execute(Iterator<Map.Entry<Listener, Executor>> it, Optional<Exception> th, Optional<R> t) {
        if(it.hasNext()) {
            Map.Entry<Listener, Executor> ex = it.next();

            ex.getValue().execute(() -> {
                try {
                    ex.getKey().run(th, t);
                } catch (Exception thh) {
                    thh.printStackTrace();
                }

                // Wait until the last function executed before running the next. This ensures they execute in the order
                // that they were registered.
                execute(it, th, t);
            });
        }
    }


    /**
     * This is a helper function that fires a function and uses
     * the Promise object as a wrapper for its result. Promises
     * don't need knowledge of the function that called them,
     * and it gives us more flexibility if they don't.
     */
    private <V> Promise<R> fire(Function<R, V> func, V val) {
        if (this.state != State.PENDING)
            throw new IllegalStateException("Promise has already been made! You cannot fire it again!");

        if (func != null) {
            this.executor.execute(() -> {
                try {
                    this.resolve(func.run(Optional.ofNullable(val)));
                } catch (Exception th) {
                    this.reject(th);
                }
            });
        }

        return this;
    }

    public static <R> Promise<R> make(R r) {
        return make(() -> r);
    }

    public static Promise<Void> make(Runnable func) {
        return make(v -> {
            func.run();
            return null;
        }, null);
    }

    public static <R> Promise<R> make(FunctionAlone<R> func) {
        return make(func, null);
    }

    public static <R> Promise<R> make(FunctionAlone<R> func, Executor exec) {
        return make(v -> func.run(), exec);
    }

    public static <R> Promise<R> make(Function<R, Void> func, Executor exec) {
        return new Promise<R>(exec).fire(func, null);
    }

    public static <T> Promise<List<T>> collect(Promise<T>... promises) {
        return collect(null, promises);
    }

    public static <T> Promise<List<T>> collect(Collection<Promise<T>> promises) {
        return collect(null, promises.toArray(new Promise[0]));
    }

    public static <T> Promise<List<T>> collect(Executor exec, Promise<T>... promises) {
        if(promises.length == 0) {
            return Promise.make(new ArrayList<>());
        }

        Promise<List<T>> promise = new Promise<>(exec);

        AtomicBoolean fail = new AtomicBoolean(false);

        Object[] successes = new Object[promises.length];
        Exception[] failures = new Exception[promises.length];

        AtomicInteger i = new AtomicInteger(promises.length);
        Runnable onFinish = () -> {
            if (i.decrementAndGet() == 0) {
                if (fail.get()) {
                    promise.reject(new RuntimeException("Some promises were rejected!"));
                    return;
                }

                List<T> success = new ArrayList<>();
                for (Object o : successes) {
                    success.add((T) o);
                }
                promise.resolve(Collections.unmodifiableList(success));
            }
        };

        for (int j = 0; j < promises.length; j++) {
            final int k = j;
            promises[j].onSuccess(val -> {
                successes[k] = val.orElse(null);
            });
        }

        for (int j = 0; j < promises.length; j++) {
            final int k = j;
            promises[j].onFailure(err -> {
                failures[k] = err;

                fail.set(true);
            });
        }

        for (int j = 0; j < promises.length; j++) {
            final int k = j;
            promises[j].on(() -> {
                onFinish.run();
            });
        }

        return promise;
    }
}