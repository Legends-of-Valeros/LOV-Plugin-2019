package com.legendsofvaleros.modules.mobs;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.PromiseCache;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collection;
import java.util.List;

public class MobsAPI extends ListenerModule {
    public interface RPC {
        Promise<List<SpawnArea>> findSpawns();

        Promise<Object> saveSpawn(SpawnArea spawn);

        Promise<Boolean> deleteSpawn(Integer spawn);

        Promise<Mob> getMob(String id);
    }

    private RPC rpc;

    private Multimap<String, SpawnArea> spawns = HashMultimap.create();

    public Collection<SpawnArea> getSpawns() {
        return spawns.values();
    }

    private Multimap<String, SpawnArea> spawnsLoaded = HashMultimap.create();

    public Collection<SpawnArea> getLoadedSpawns() {
        return spawnsLoaded.values();
    }

    private PromiseCache<String, Mob> mobs;

    public Mob getMob(String id) {
        return mobs.getIfPresent(id);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        this.mobs = new PromiseCache<>(CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakValues()
                .removalListener(entry -> {
                    getLogger().warning("Mob '" + entry.getKey() + "' removed from the cache: " + entry.getCause());
                })
                .build(), id -> rpc.getMob(id));

        InterfaceTypeAdapter.register(IEntity.class,
                obj -> obj.getId(),
                id -> mobs.get(id).next(v -> Promise.make(v.orElse(null))));

        registerEvents(new ChunkListener());
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        this.loadAll().get();
    }

    public Promise loadAll() {
        return rpc.findSpawns().onSuccess(val -> {
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

        if (spawn.getEntity() != null)
            spawn.getEntity().getSpawns().add(spawn);
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
