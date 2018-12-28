package com.legendsofvaleros.modules.mobs;

import com.codingforcookies.doris.orm.ORMTable;
import com.codingforcookies.doris.sql.QueryMethod;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.modules.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnManager {
    private static final Collection<SpawnArea> EMPTY = ImmutableList.of();

    private static ORMTable<SpawnArea> spawnsTable;

    private static Multimap<String, SpawnArea> spawns = HashMultimap.create();
    public static Collection<SpawnArea> getSpawns() {
        return spawns.values();
    }

    private static volatile AtomicInteger misses = new AtomicInteger(0),
            loaded = new AtomicInteger(0),
            unloaded = new AtomicInteger(0);

    private static Cache<String, Collection<SpawnArea>> cachedSpawns = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .removalListener(entry -> {
                // Ignore replacements
                if(entry.getCause() == RemovalCause.REPLACED) return;

                unloaded.addAndGet(((Collection<SpawnArea>) entry.getValue()).size());

                // Mobs.getInstance().getLogger().warning("Chunk '" + entry.getKey() + "' removed from the cache: " + entry.getCause());
            })
            .build();

    public static void onEnable() {
        Mobs.getInstance().registerEvents(new SpawnsListener());

        spawnsTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), SpawnArea.class);

        Mobs.getInstance().getScheduler().executeInMyCircleTimer(() -> {
            // This is done so we get almost-live updates on GC'd listeners.
            cachedSpawns.cleanUp();
        }, 0L, 20L);

        Mobs.getInstance().getScheduler().executeInMyCircleTimer(() -> {
            if (misses.get() > 0) {
                Mobs.getInstance().getLogger().info(misses + " chunk misses.");
            }

            if (loaded.get() > 0) {
                Mobs.getInstance().getLogger().info("Loaded " + loaded + " spawns.");
            }
            if (unloaded.get() > 0) {
                Mobs.getInstance().getLogger().info("Unloaded " + unloaded + " spawns.");
            }

            misses.set(0);
            loaded.set(0);
            unloaded.set(0);
        }, 20L * 60L * 60L, 20L * 60L * 60L);
    }

    public static void addSpawn(SpawnArea spawn) {
        Chunk chunk = spawn.getLocation().getChunk();

        spawns.put(getId(chunk), spawn);

        // If editing is enabled, generate the hologram right away.
        if(LegendsOfValeros.getMode().allowEditing())
            Mobs.getInstance().getScheduler().sync(spawn::getHologram);

        ListenableFuture<Mob> future = MobManager.loadEntity(spawn.getEntityId());
        future.addListener(() -> {
            try {
                Mob mob = future.get();
                if (mob != null) {
                    spawn.getMob(); // Get the spawn to save its Mob object

                    mob.getSpawns().add(spawn);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, Mobs.getInstance().getScheduler()::async);
    }

    public static void updateSpawn(final SpawnArea spawn) {
        if (spawn == null)
            return;

        spawnsTable.save(spawn, true);
    }

    public static void removeSpawn(final SpawnArea spawn) {
        if (spawn == null)
            return;

        spawn.delete();

        spawnsTable.delete(spawn, true);
    }

    private static String getId(Chunk chunk) {
        return chunk.getX() + "," + chunk.getZ();
    }

    private static class SpawnsListener implements Listener {
        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            String chunkId = getId(event.getChunk());

            Collection<SpawnArea> cached = cachedSpawns.getIfPresent(chunkId);
            if (cached != null) {
                if(cached.size() == 0) return;

                for (SpawnArea spawn : cached)
                    addSpawn(spawn);

                // Set the cache to empty to save a marginal amount of memory
                cachedSpawns.put(chunkId, EMPTY);
            } else {
                // Cache spawns so they aren't requeried while this query runs
                cachedSpawns.put(chunkId, EMPTY);

                spawnsTable.query()
                        .select()
                        .where("spawn_world", event.getWorld().getName(),
                                "spawn_x", QueryMethod.GREATER_THAN_EQ, event.getChunk().getX() * 16,
                                "spawn_x", QueryMethod.LESS_THAN, (event.getChunk().getX() * 16 + 16),
                                "spawn_z", QueryMethod.GREATER_THAN_EQ, event.getChunk().getZ() * 16,
                                "spawn_z", QueryMethod.LESS_THAN, (event.getChunk().getZ() * 16 + 16))
                        .build()
                        .callback((result, count) -> {
                            if (count != null)
                                loaded.addAndGet(count);
                        })
                        .forEach((spawn, i) -> addSpawn(spawn))
                        .onEmpty(() -> misses.incrementAndGet())
                        .execute(true);
            }
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            String chunkId = getId(event.getChunk());

            // Put the chunk spawns in the temporary cache
            if (spawns.containsKey(chunkId)) {
                Collection<SpawnArea> spawns = SpawnManager.spawns.get(chunkId);

                cachedSpawns.put(chunkId, new ArrayList<>(spawns));

                for (SpawnArea spawn : spawns)
                    spawn.delete();
            }

            spawns.removeAll(chunkId);
        }
    }
}