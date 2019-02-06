package com.legendsofvaleros.api;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import io.deepstream.DeepstreamClient;
import io.deepstream.RpcResult;
import org.apache.http.concurrent.FutureCallback;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

public class RPCFunction<T> {
    public interface RPCCallback<T> {
        void run(T t);
    }

    public static RPCFunction create(String func) {
        return new RPCFunction(func);
    }

    public static String getPrefix(AnnotatedElement e) {
        String prefix = "";

        // Turns the @ModuleRPC("characters") method from getCharacter
        // into characters:getCharacter
        if(e.getAnnotation(ModuleRPC.class) != null) {
            prefix += e.getAnnotation(ModuleRPC.class).value();
            if (prefix.length() > 0) prefix += ":";
        }

        return prefix;
    }

    public static String getMethodName(Method m) {
        String name = getPrefix(m);
        name += m.getName();
        return name;
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

    public void call() { call(null, null); }
    public void call(RPCCallback<T> callback) {
        call(null, new FutureCallback<T>() {
            @Override
            public void completed(T t) {
                callback.run(t);
            }

            @Override public void failed(Exception e) { }

            @Override public void cancelled() { }
        });
    }

    public void call(FutureCallback<T> callback) { call(null, callback); }

    public void call(Object arg, FutureCallback<T> callback) {
        executor.execute(() -> {
            try {
                callback.completed(callSync(arg));
            } catch(Exception e) {
                callback.failed(e);
            }
        });
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

        if(retType.isAssignableFrom(ListenableFuture.class)) {
            SettableFuture<Object> ret = SettableFuture.create();

            call(arg, new FutureCallback<T>() {
                @Override
                public void completed(T t) {
                    ret.set(t);
                }

                @Override
                public void failed(Exception e) {
                    ret.setException(e);
                }

                @Override
                public void cancelled() { }
            });

            return ret;
        }

        return callSync(arg);
    }
}
