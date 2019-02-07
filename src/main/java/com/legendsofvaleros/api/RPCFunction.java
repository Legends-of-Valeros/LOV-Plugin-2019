package com.legendsofvaleros.api;

import com.legendsofvaleros.api.annotation.ModuleRPC;
import io.deepstream.DeepstreamClient;
import io.deepstream.RpcResult;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class RPCFunction<T> {
    public interface RPCCallback<T> {
        void run(Exception e, T t);
    }

    public static RPCFunction create(String func) {
        return new RPCFunction(func);
    }

    public static String getMethodName(Method m) {
        StringBuilder name = new StringBuilder();

        Class<?> c = m.getDeclaringClass();

        if(c.getAnnotation(ModuleRPC.class) != null) {
            name.append(c.getAnnotation(ModuleRPC.class).value());
            if (name.length() > 0) name.append(":");
        }

        if(m.getAnnotation(ModuleRPC.class) != null)
            name.append(m.getAnnotation(ModuleRPC.class).value());
        else
            name.append(m.getName());

        return name.toString();
    }

    private final Executor executor;

    private final DeepstreamClient client;
    private final String func;

    public RPCFunction(Method m) { this(null, getMethodName(m)); }
    public RPCFunction(String func) { this(null, func); }

    public RPCFunction(Executor executor, Method m) { this(executor, getMethodName(m)); }
    public RPCFunction(Executor executor, String func) {
        this.executor = executor != null ? executor : APIController.getInstance().getPool();

        this.client = APIController.getInstance().getClient();
        this.func = func;
    }

    public Promise<T> call() { return call(null); }

    public Promise<T> call(Object arg) {
        Promise<T> promise = new Promise<>(executor);

        executor.execute(() -> {
            try {
                promise.resolve(callSync(arg));
            } catch(Exception e) {
                promise.reject(e);
            }
        });

        return promise;
    }

    public T callSync() {
        return callSync(null);
    }

    public T callSync(Object arg) {
        RpcResult result = client.rpc.make(func, arg);
        if(result.success())
            return (T)result.getData();
        throw new RuntimeException(func + "() failed: " + result.getData());
    }

    public Object callInternal(Method m, T arg) {
        Class<?> retType = m.getReturnType();

        if(Promise.class.isAssignableFrom(retType))
            return call(arg);

        return callSync(arg);
    }
}
