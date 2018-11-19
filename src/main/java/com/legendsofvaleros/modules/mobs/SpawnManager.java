package com.legendsofvaleros.modules.mobs;

import com.codingforcookies.doris.orm.ORMTable;
import com.codingforcookies.doris.sql.QueryMethod;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.Utilities;
import com.legendsofvaleros.util.event.ToggleOpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnManager {
    private static final Collection<SpawnArea> EMPTY = new ArrayList<>();

    private static ORMTable<SpawnArea> spawnsTable;

    private static Multimap<Chunk, SpawnArea> spawns = HashMultimap.create();

    public static Collection<SpawnArea> getSpawns() {
        return spawns.values();
    }

    private static volatile AtomicInteger misses = new AtomicInteger(0),
            loaded = new AtomicInteger(0),
            unloaded = new AtomicInteger(0);

    private static Cache<Chunk, Collection<SpawnArea>> cachedSpawns = CacheBuilder.newBuilder()
            .expireAfterAccess(10L, TimeUnit.MINUTES)
            .removalListener((entry) -> {
                if (entry.getValue() == null) return;
                for (SpawnArea spawn : (Collection<SpawnArea>) entry.getValue()) {
                    unloaded.incrementAndGet();
                    spawn.delete();
                }
            })
            .build();

    public static void onEnable() {
        Bukkit.getPluginManager().registerEvents(new SpawnsListener(), LegendsOfValeros.getInstance());

        spawnsTable = ORMTable.bind(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"), SpawnArea.class);

        Bukkit.getScheduler().runTaskTimerAsynchronously(LegendsOfValeros.getInstance(), () -> {
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

    public static void addSpawn(Chunk chunk, SpawnArea spawn) {
        if (chunk == null)
            chunk = spawn.getWorld().getChunkAt(spawn.getLocation());

        spawns.put(chunk, spawn);

        ListenableFuture<Mob> future = spawn.loadMob();
        future.addListener(() -> {
            try {
                Mob mob = future.get();
                if (mob != null)
                    mob.getSpawns().add(spawn);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, Utilities.asyncExecutor());
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

    private static class SpawnsListener implements Listener {
        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            Collection<SpawnArea> cached = cachedSpawns.getIfPresent(event.getChunk());
            if (cached != null) {
                for (SpawnArea spawn : cached)
                    addSpawn(event.getChunk(), spawn);
            } else {
                // Cache spawns so they aren't requeried for some time
                cachedSpawns.put(event.getChunk(), EMPTY);

                spawnsTable.query()
                        .select()
                        .where("spawn_world", event.getWorld().getName(),
                                "spawn_x", QueryMethod.GREATER_THAN, event.getChunk().getX() * 16,
                                "spawn_x", QueryMethod.LESS_THAN, (event.getChunk().getX() * 16 + 16),
                                "spawn_z", QueryMethod.GREATER_THAN, event.getChunk().getZ() * 16,
                                "spawn_z", QueryMethod.LESS_THAN, (event.getChunk().getZ() * 16 + 16))
                        .build()
                        .callback((result) -> {
                            if (result.last()) {
                                loaded.addAndGet(result.getRow());
                                result.beforeFirst();
                            }
                        })
                        .forEach(spawn -> addSpawn(event.getChunk(), spawn))
                        .onEmpty(() -> misses.incrementAndGet())
                        .execute(true);
            }

        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            // Put the chunk spawns in the temporary cache
            if (spawns.containsKey(event.getChunk()))
                cachedSpawns.put(event.getChunk(), spawns.get(event.getChunk()));

            spawns.removeAll(event.getChunk());
        }

        @EventHandler
        public void onToggleOp(ToggleOpEvent event) {
            for (SpawnArea spawn : getSpawns()) {
                if (event.isOp()) {
                    spawn.getHologram().getVisibilityManager().showTo(event.getPlayer());
                } else {
                    spawn.getHologram().getVisibilityManager().hideTo(event.getPlayer());
                }
            }
        }
    }
}