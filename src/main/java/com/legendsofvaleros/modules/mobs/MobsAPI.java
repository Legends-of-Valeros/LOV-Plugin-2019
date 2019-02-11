package com.legendsofvaleros.modules.mobs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
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

    private Map<String, Mob> entities = new HashMap<>();
    public Mob getEntity(String id) { return entities.get(id); }

    private Multimap<String, SpawnArea> spawns = HashMultimap.create();
    public Collection<SpawnArea> getSpawns() {
        return spawns.values();
    }

    private Multimap<String, SpawnArea> spawnsLoaded = HashMultimap.create();
    public Collection<SpawnArea> getLoadedSpawns() {
        return spawnsLoaded.values();
    }

    public MobsAPI() {
        this.rpc = APIController.create(MobsController.getInstance(), RPC.class);

        MobsController.getInstance().registerEvents(new ChunkListener());
    }

    public Promise loadAll() {
        return rpc.find().onSuccess(val -> {
                    entities.clear();

                for(Mob mob : val)
                    entities.put(mob.getId(), mob);

                LootController.getInstance().getLogger().info("Loaded " + entities.size() + " mobs.");
            }).onFailure(Throwable::printStackTrace)
            .next(rpc::findSpawns).onSuccess(val -> {
                spawns.clear();

                // Return to the spigot thread to allow fetching chunk objects.
                // We could remove all of this, basically, if we just make a way
                // to get the chunk ID from a block location, this can be async.
                MobsController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                    for(SpawnArea spawn : val) {
                        spawns.put(getId(spawn.getLocation().getChunk()), spawn);

                        addSpawn(spawn);
                    }

                    LootController.getInstance().getLogger().info("Loaded " + spawns.size() + " spawns.");
                });
            }).onFailure(Throwable::printStackTrace);
    }

    public void addSpawn(SpawnArea spawn) {
        Chunk chunk = spawn.getLocation().getChunk();

        spawns.put(getId(chunk), spawn);

        // If editing is enabled, generate the hologram right away.
        if(LegendsOfValeros.getMode().allowEditing())
            MobsController.getInstance().getScheduler().sync(spawn::getHologram);

        Mob mob = entities.get(spawn.getEntityId());
        if (spawn.getMob() != null)
            mob.getSpawns().add(spawn);
    }

    public void updateSpawn(final SpawnArea spawn) {
        if (spawn == null)
            return;

        // spawnsTable.save(spawn, true);
    }

    public void removeSpawn(final SpawnArea spawn) {
        if (spawn == null)
            return;

        spawn.delete();

        // spawnsTable.delete(spawn, true);
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
