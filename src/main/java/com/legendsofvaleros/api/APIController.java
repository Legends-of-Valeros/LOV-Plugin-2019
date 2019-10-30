package com.legendsofvaleros.api;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import io.deepstream.ConfigOptions;
import io.deepstream.DeepstreamClient;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Duration;
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

    private DeepstreamClient client;

    public DeepstreamClient getClient() {
        return client;
    }

    private JsonObject apiSession;

    public JsonObject getAPISession() { return apiSession; }

    private Set<String> rpcFuncs = new HashSet<>();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        this.pool = Executors.newCachedThreadPool();

        this.gsonBuilder.registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) -> Duration.parse(json.getAsString()));

        this.gsonBuilder.registerTypeHierarchyAdapter(CharacterId.class, new TypeAdapter<CharacterId>() {
            @Override
            public void write(JsonWriter write, CharacterId characterId) throws IOException {
                if (characterId == null) {
                    write.nullValue();
                    return;
                }
                write.value(characterId.toString());
            }

            @Override
            public CharacterId read(JsonReader read) throws IOException {
                if (read.peek() == JsonToken.NULL) {
                    read.nextNull();
                    return null;
                }

                return CharacterId.fromString(read.nextString());
            }
        });

        this.gsonBuilder.registerTypeHierarchyAdapter(PlayerCharacter.class, new TypeAdapter<PlayerCharacter>() {
            @Override
            public void write(JsonWriter write, PlayerCharacter pc) throws IOException {
                write.value(pc != null ? pc.getUniqueCharacterId().toString() : null);
            }

            @Override
            public PlayerCharacter read(JsonReader read) throws IOException {
                // If we reference the interface, then the type should be a string, and we return the stored object.
                // Note: it must be loaded already, else this returns null.
                if(read.peek() == JsonToken.NULL) {
                    read.nextNull();
                    return null;
                }

                return Characters.getPlayerCharacter(CharacterId.fromString(read.nextString()));
            }
        });

        this.gsonBuilder.registerTypeHierarchyAdapter(World.class, new TypeAdapter<World>() {
            @Override
            public void write(JsonWriter write, World world) throws IOException {
                write.value(world.getName());
            }

            @Override
            public World read(JsonReader read) throws IOException {
                if(read.peek() == JsonToken.NULL) {
                    read.nextNull();
                    return null;
                }

                return Bukkit.getWorld(read.nextString());
            }
        });

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(Location.class, new TypeAdapter<Location>() {
                    @Override
                    public void write(JsonWriter write, Location loc) throws IOException {
                        write.beginObject();

                        write.name("x");
                        write.value(loc.getX());

                        write.name("y");
                        write.value(loc.getY());

                        write.name("z");
                        write.value(loc.getZ());

                        write.name("yaw");
                        write.value(loc.getYaw());

                        write.name("pitch");
                        write.value(loc.getPitch());

                        write.endObject();
                    }

                    @Override
                    public Location read(JsonReader read) throws IOException {
                        // If we reference the interface, then the type should be a string, and we return the stored object.
                        // Note: it must be loaded already, else this returns null.
                        if(read.peek() == JsonToken.NULL) {
                            read.nextNull();
                            return null;
                        }

                        Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);

                        read.beginObject();

                        while(read.peek() != JsonToken.END_OBJECT) {
                            switch(read.nextName()) {
                                case "x":
                                    loc.setX(read.nextDouble());
                                    break;
                                case "y":
                                    loc.setY(read.nextDouble());
                                    break;
                                case "z":
                                    loc.setZ(read.nextDouble());
                                    break;
                                case "yaw":
                                    loc.setYaw((float)read.nextDouble());
                                    break;
                                case "pitch":
                                    loc.setPitch((float)read.nextDouble());
                                    break;
                            }
                        }

                        read.endObject();

                        return loc;
                    }
                });
        try {
            Map<String, Object> opts = new HashMap<>();
            opts.put(ConfigOptions.RPC_RESPONSE_TIMEOUT.toString(), "30000");

            ConfigurationSection section = LegendsOfValeros.getInstance().getConfig().getConfigurationSection("api");
            {
                // We use 6021 instead of 6020 due to the DS compat layer
                String endpoint = section.getString("endpoint", "127.0.0.1:6021");

                getLogger().info("Connecting to API at " + endpoint);
                this.client = new DeepstreamClient(endpoint, opts);

                JsonObject authParams = new JsonObject();

                authParams.add("token", new JsonPrimitive(section.getString("token", "")));

                Object ret = this.client.login(authParams).getData();

                if(ret instanceof JsonObject) {
                    apiSession = ((JsonObject)ret).getAsJsonObject("session");
                }else{
                    throw new RuntimeException("Unknown value returned from Deepstream login: " + ret);
                }

                getLogger().info("Logged in to Deepstream successfully");
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
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

            if (!err) {
                getLogger().info("No issues! All registered RPC functions exist!");
            }
        });
    }

    /**
     * You should use the fromJson or toJson methods in this class, rather than accessing Gson directly.
     */
    @Deprecated
    public Gson getGson() {
        return this.gson;
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T> fromJson(JsonElement elem, Type type) {
        if(this.gson == null)
            throw new IllegalStateException("Must wait until onPostLoad() before using RPC functions!");
        return Promise.make(() -> this.gson.fromJson(elem, type));
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T> fromJson(String json, Type type) {
        if(this.gson == null)
            throw new IllegalStateException("Must wait until onPostLoad() before using RPC functions!");
        return Promise.make(() -> this.gson.fromJson(json, type));
    }

    public <T> Promise<T> fromJson(JsonElement elem, Class<T> clazz) {
        if(this.gson == null)
            throw new IllegalStateException("Must wait until onPostLoad() before using RPC functions!");
        return Promise.make(() -> this.gson.fromJson(elem, clazz));
    }

    public <T> Promise<T> fromJson(String json, Class<T> clazz) {
        if(this.gson == null)
            throw new IllegalStateException("Must wait until onPostLoad() before using RPC functions!");
        return Promise.make(() -> this.gson.fromJson(json, clazz));
    }

    public Promise<String> toJson(Object arg) {
        return Promise.make(() -> this.gson.toJson(arg));
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

        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new java.lang.Class[]{ clazz },
                (proxy, m, args) -> methods.get(m.getName()).call((args != null ? args : new Object[0]))
        ));
    }
}