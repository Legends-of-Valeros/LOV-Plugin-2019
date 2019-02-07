package com.legendsofvaleros.api;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import io.deepstream.DeepstreamClient;

import java.lang.reflect.Method;
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


            api.ping().on((err, val) -> {
                if(err != null) {
                    err.printStackTrace();
                    return;
                }

                System.out.println("async ping");
            });

            System.out.println("sync ping");
            System.out.println(api.pingSync());
            System.out.println("sync pong");

            new RPCFunction(getScheduler()::async, "ping").call()
                .on((err, val) -> {
                if(err != null) {
                    err.printStackTrace();
                    return;
                }

                System.out.println("manual pong");
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static <T> T create(Class<T> clazz) {
        return create((Executor)null, clazz);
    }

    private static <T> T create(Module module, Class<T> clazz) {
        return create(module.getScheduler()::async, clazz);
    }

    private static <T> T create(Executor executor, Class<T> clazz) {
        Map<String, RPCFunction> methods = new HashMap<>();

        for(Method m : clazz.getDeclaredMethods()) {
            if(m.getParameterTypes().length > 1)
                throw new IllegalArgumentException("RPC interfaces can only a maximum of one argument.");
            methods.put(m.getName(), new RPCFunction<>(executor, m));
        }

        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new java.lang.Class[] { clazz },
            (proxy, m, args) -> {
                if(args != null && args.length > 1)
                    throw new IllegalArgumentException("Too many arguments!");

                return methods.get(m.getName()).callInternal(m, (args != null && args.length == 1 ? args[0] : null));
            }
        );
    }
}