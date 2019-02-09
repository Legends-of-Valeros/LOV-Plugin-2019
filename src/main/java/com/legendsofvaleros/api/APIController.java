package com.legendsofvaleros.api;

import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import io.deepstream.DeepstreamClient;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ModuleInfo(name = "API", info = "")
public class APIController extends Module {
    private static APIController instance;
    public static APIController getInstance() {
        return instance;
    }

    private Executor pool;
    public Executor getPool() { return pool; }

    private DeepstreamClient client;
    public DeepstreamClient getClient() { return client; }

    APITest api;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        this.pool = Executors.newFixedThreadPool(8);

        try {
            client = new DeepstreamClient("localhost:6020");

            client.login();

            api = APIController.create(APITest.class);

            Promise promise = api.ping().on((err, val) -> {
                if(err != null) {
                    err.printStackTrace();
                    return;
                }

                System.out.println("async ping");
            });

            promise = promise.then(() -> false)
                    .onSuccess((val) -> System.out.println("1: " + val))
                    .onFailure((err) -> ((Throwable)err).printStackTrace());

            promise = promise.then(() -> {
                return api.ping()
                        .onSuccess((val) -> System.out.println("2: " + val))
                        .onFailure((err) -> err.printStackTrace());
            });

            promise = promise.next(api::ping)
                    .onSuccess((val) -> System.out.println("3: " + val))
                    .onFailure((err) -> ((Throwable)err).printStackTrace());

            try {
                System.out.println("4: " + Promise.collect(Promise.make(() -> 0), Promise.make(() -> 1)).get());
            } catch (Throwable th) {
                th.printStackTrace();
            }

            Promise.collect(Promise.make(() -> 0), Promise.make(() -> {
                throw new Exception("test");
            })).onFailure((err) -> {
                err.printStackTrace();
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static <T> T create(Class<T> clazz) {
        return create((Executor)null, clazz);
    }

    public static <T> T create(Module module, Class<T> clazz) {
        return create(module.getScheduler()::async, clazz);
    }

    public static <T> T create(Executor executor, Class<T> clazz) {
        Map<String, RPCFunction> methods = new HashMap<>();

        for(Method m : clazz.getDeclaredMethods())
            methods.put(m.getName(), RPCFunction.create(executor, m));

        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new java.lang.Class[] { clazz },
            (proxy, m, args) -> methods.get(m.getName()).call((args != null ? args : new Object[0]))
        ));
    }
}