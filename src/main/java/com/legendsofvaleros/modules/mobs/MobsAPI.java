package com.legendsofvaleros.modules.mobs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.modules.loot.LootController;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MobsAPI {
    @ModuleRPC("mobs")
    public interface RPC {
        Promise<Mob[]> find();
        Promise<SpawnArea[]> findSpawns();
    }

    private final RPC rpc;

    private Map<String, Mob> mobs = new HashMap<>();
    public Mob getMob(String id) { return mobs.get(id); }

    private static Multimap<String, SpawnArea> spawns = HashMultimap.create();
    public static Collection<SpawnArea> getSpawns() {
        return spawns.values();
    }

    private static Multimap<String, SpawnArea> spawnsLoaded = HashMultimap.create();

    public MobsAPI() {
        this.rpc = APIController.create(MobsController.getInstance(), RPC.class);

        MobsController.getInstance().registerEvents(new ChunkListener());
    }

    public Promise<Object[]> loadAll() {
        return Promise.collect(rpc.find().onSuccess(val -> {
                mobs.clear();

                for(Mob mob : val)
                    mobs.put(mob.getId(), mob);

                LootController.getInstance().getLogger().info("Loaded " + mobs.size() + " mobs.");
            }).onFailure(Throwable::printStackTrace),
            rpc.findSpawns().onSuccess(val -> {
                spawns.clear();

                for(SpawnArea spawn : val)
                    spawns.put(getId(spawn.getLocation().getChunk()), spawn);

                LootController.getInstance().getLogger().info("Loaded " + spawns.size() + " spawns.");
            }).onFailure(Throwable::printStackTrace)
        );
    }

    private String getId(Chunk chunk) {
        return chunk.getX() + "," + chunk.getZ();
    }

    private class ChunkListener implements Listener {
        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            String chunkId = getId(event.getChunk());

            if(!spawns.containsKey(chunkId)) return;

            spawnsLoaded.putAll(chunkId, spawns.get(chunkId));
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            spawnsLoaded.removeAll(getId(event.getChunk()));
        }
    }
}
