package com.legendsofvaleros.api;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import io.deepstream.ConfigOptions;
import io.deepstream.DeepstreamClient;
import io.deepstream.InvalidDeepstreamConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ModuleInfo(name = "API", info = "")
public class APIController extends Module {
    private static APIController instance;

    public static APIController getInstance() {
        return instance;
    }

    private Executor pool;

    public Executor getPool() {
        return pool;
    }

    private GsonBuilder gsonBuilder = new GsonBuilder();

    public GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }

    private Gson gson;

    public Gson getGson() {
        return gson;
    }

    private DeepstreamClient client;

    public DeepstreamClient getClient() {
        return client;
    }

    private Set<String> rpcFuncs = new HashSet<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        this.pool = Executors.newFixedThreadPool(8);

        this.gsonBuilder.registerTypeAdapter(CharacterId.class, new TypeAdapter<CharacterId>() {
            @Override
            public void write(JsonWriter write, CharacterId characterId) throws IOException {
                write.value(characterId.toString());
            }

            @Override
            public CharacterId read(JsonReader read) throws IOException {
                if(read.peek() == JsonToken.NULL) {
                    read.nextNull();
                    return null;
                }
                return CharacterId.fromString(read.nextString());
            }
        });

        TypeAdapter<World> worldType = new TypeAdapter<World>() {
            @Override
            public void write(JsonWriter write, World world) throws IOException {
                write.value(world.getName());
            }

            @Override
            public World read(JsonReader read) throws IOException {
                return Bukkit.getWorld(read.nextString());
            }
        };
        this.gsonBuilder.registerTypeAdapter(World.class, worldType);
        this.gsonBuilder.registerTypeAdapter(CraftWorld.class, worldType);

        try {
            Map<String, Object> opts = new HashMap<>();
            opts.put(ConfigOptions.RPC_RESPONSE_TIMEOUT.toString(), "30000");

            String apiEndpoint = LegendsOfValeros.getInstance().getConfig().getString("api-endpoint", "127.0.0.1:6020");
            this.client = new DeepstreamClient(apiEndpoint, opts);

            this.client.login();
        } catch (URISyntaxException | InvalidDeepstreamConfig e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        // Doing this in onPostLoad allows other modules to register
        // decoders for the API system.
        this.gson = this.gsonBuilder.create();

        // Do a quick verify of methods registered
        getScheduler().executeInSpigotCircle(() -> {
            boolean err = false;

            Map<String, Boolean> bools = RPCFunction.oneShotSync("ifMethodExists", TypeToken.of(Map.class), rpcFuncs);
            for (Map.Entry<String, Boolean> entry : bools.entrySet()) {
                if (!entry.getValue()) {
                    err = true;
                    getLogger().severe(entry.getKey() + "() is not known in the API!");
                }
            }

            if (!err)
                getLogger().info("No issues! All registered RPC functions exist!");
        });

        /*Promise<?> promise = api.ping().on((err, val) -> {
            if(err != null) {
                err.printStackTrace();
                return;
            }

            System.out.println("async ping");
        });

        promise = promise.then(() -> false)
                .onSuccess((val) -> System.out.println("1: " + val))
                .onFailure((err) -> err.printStackTrace());

        promise = promise.then(() -> {
            return api.ping()
                    .onSuccess((val) -> System.out.println("2: " + val))
                    .onFailure((err) -> err.printStackTrace());
        });

        promise = promise.next(api::ping)
                .onSuccess((val) -> System.out.println("3: " + val))
                .onFailure((err) -> err.printStackTrace());

        promise = promise.next(() -> this.api.find(UUID.randomUUID()))
                    .onSuccess((val) -> System.out.println("UUID: " + val))
                    .onFailure((err) -> err.printStackTrace());

        try {
            System.out.println("4: " + Promise.collect(Promise.make(() -> 0), Promise.make(() -> 1)).get());
        } catch (Throwable th) {
            th.printStackTrace();
        }

        Promise.collect(Promise.make(() -> 0), Promise.make(() -> {
            throw new Exception("test");
        })).onFailure((err) -> {
            err.printStackTrace();
        });*/
    }

    public static <T> T create(Class<T> clazz) {
        return create(getInstance().getPool(), clazz);
    }

    public static <T> T create(Module module, Class<T> clazz) {
        return create(module.getScheduler()::async, clazz);
    }

    public static <T> T create(Executor executor, Class<T> clazz) {
        Map<String, RPCFunction> methods = new HashMap<>();

        RPCFunction rpc;
        for (Method m : clazz.getDeclaredMethods()) {
            methods.put(m.getName(), rpc = RPCFunction.create(executor, m));
            getInstance().rpcFuncs.add(rpc.getName());
        }

        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new java.lang.Class[]{clazz},
                (proxy, m, args) -> methods.get(m.getName()).call((args != null ? args : new Object[0]))
        ));
    }
}