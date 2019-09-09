package com.legendsofvaleros.modules.mobs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.mobs.api.IMob;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.modules.npcs.api.INPC;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobsAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Mob>> findMobs();

        Promise<List<SpawnArea>> findSpawns();

        Promise<Object> saveSpawn(SpawnArea spawn);

        Promise<Boolean> deleteSpawn(Integer spawn);
    }

    private RPC rpc;

    private Map<String, Mob> entities = new HashMap<>();

    public Mob getEntity(String id) {
        return entities.get(id);
    }

    private Multimap<String, SpawnArea> spawns = HashMultimap.create();

    public Collection<SpawnArea> getSpawns() {
        return spawns.values();
    }

    private Multimap<String, SpawnArea> spawnsLoaded = HashMultimap.create();

    public Collection<SpawnArea> getLoadedSpawns() {
        return spawnsLoaded.values();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        APIController.getInstance().getGsonBuilder()
                .registerTypeAdapter(INPC.class, new TypeAdapter<IMob>() {
                    @Override
                    public void write(JsonWriter write, IMob mob) throws IOException {
                        write.value(mob != null ? mob.getId() : null);
                    }

                    @Override
                    public IMob read(JsonReader read) throws IOException {
                        // If we reference the interface, then the type should be a string, and we return the stored object.
                        // Note: it must be loaded already, else this returns null.
                        if(read.peek() == JsonToken.NULL) {
                            read.nextNull();
                            return null;
                        }

                        return entities.get(read.nextString());
                    }
                });

        registerEvents(new ChunkListener());
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Promise loadAll() {
        return rpc.findMobs().onSuccess(val -> {
            entities.clear();

            for (Mob mob : val.orElse(ImmutableList.of()))
                entities.put(mob.getId(), mob);

            getLogger().info("Loaded " + entities.size() + " mobs.");
        }).next(rpc::findSpawns).onSuccess(val -> {
            spawns.clear();

            // Return to the spigot thread to allow fetching chunk objects.
            // We could remove all of this, basically, if we just make a way
            // to get the chunk ID from a block location, this can be async.
            getScheduler().executeInSpigotCircle(() -> {
                for (SpawnArea spawn : val.orElse(ImmutableList.of())) {
                    spawns.put(getId(spawn.getLocation().getChunk()), spawn);

                    addSpawn(spawn);
                }

                getLogger().info("Loaded " + spawns.size() + " spawns.");
            });
        });
    }

    public void addSpawn(SpawnArea spawn) {
        Chunk chunk = spawn.getLocation().getChunk();

        spawns.put(getId(chunk), spawn);
        spawnsLoaded.put(getId(chunk), spawn);

        // If editing is enabled, generate the hologram right away.
        if (LegendsOfValeros.getMode().allowEditing())
            getScheduler().sync(spawn::getHologram);

        if (spawn.getMob() != null)
            spawn.getMob().getSpawns().add(spawn);
    }

    public void updateSpawn(SpawnArea spawn) {
        if (spawn == null)
            return;

        rpc.saveSpawn(spawn);
    }

    public void removeSpawn(final SpawnArea spawn) {
        if (spawn == null)
            return;

        spawn.delete();

        rpc.deleteSpawn(spawn.getId());
    }

    private String getId(Chunk chunk) {
        return chunk.getX() + "," + chunk.getZ();
    }

    private class ChunkListener implements Listener {
        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            String chunkId = getId(event.getChunk());

            if (!spawns.containsKey(chunkId)) {
                return;
            }

            spawnsLoaded.putAll(chunkId, spawns.get(chunkId));
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            String chunkId = getId(event.getChunk());

            if (!spawns.containsKey(chunkId)) {
                return;
            }

            spawnsLoaded.removeAll(getId(event.getChunk()));
        }
    }
}
