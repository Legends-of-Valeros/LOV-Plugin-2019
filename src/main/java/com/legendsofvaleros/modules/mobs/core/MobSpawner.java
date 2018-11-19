package com.legendsofvaleros.modules.mobs.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.mobs.Mobs;
import com.legendsofvaleros.modules.mobs.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobSpawner {
    private final Random rand = new Random();

    private final int allUpdateInterval;

    private int repopulated = 0;
    private int unloaded = 0;
    private int distance = 0;

    public MobSpawner() {
        this.allUpdateInterval = Mobs.getInstance().getConfig().getInt("spawn-area-update-smear", 20 * 10);

        Mobs.getInstance().getLogger().info("Smearing spawn updates across " + allUpdateInterval + " ticks.");

        new BukkitRunnable() {
            private long time = 0;

            @Override
            public void run() {
                updateSpawn(time++);
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 1, 1);
    }

    private void updateSpawn(long time) {
        int block = (int) (time % allUpdateInterval);
        int blockSize = (int) Math.ceil((double) SpawnManager.getSpawns().size() / allUpdateInterval);

        SpawnManager.getSpawns().stream()
                .skip(block * blockSize).limit(blockSize)
                .forEach(spawn -> {
                    World world = spawn.getWorld();
                    Location spawnLocation = spawn.getLocation();

                    if (!spawnLocation.getChunk().isLoaded()) {
                        if (spawn.getEntities().size() > 0) {
                            Mobs.getInstance().getLogger().info("Clearing spawn due to unloaded chunk.");
                            unloaded++;

                            spawn.clear();
                        }

                        return;
                    }

                    boolean playerNearby = false;

                    List<org.bukkit.entity.Entity> entities = new ArrayList<>(world.getNearbyEntities(spawnLocation, spawn.getRadius() + 25, spawn.getRadius() + 20, spawn.getRadius() + 25));
                    CombatEntity ce;
                    for (org.bukkit.entity.Entity e : entities) {
                        if (!(e instanceof LivingEntity)) continue;
                        ce = CombatEngine.getEntity((LivingEntity) e);
                        if (ce != null && ce.isPlayer()) {
                            playerNearby = true;
                            break;
                        }
                    }

                    if (!playerNearby) {
                        if (spawn.getEntities().size() > 0) {
                            distance++;

                            spawn.clear();
                        }

                        return;
                    }

                    Mob mob = spawn.getMob();
                    if (mob == null) {
                        Bukkit.broadcastMessage(ChatColor.RED + "[!] Unknown instance ID. Offender: " + spawn.getEntityId() + " at " + spawn.getLocation());
                        return;
                    }

                    if (spawn.getDespawnedEnemies() > 0) {
                        repopulated++;

                        spawn.repopulated();

                        int entityCount = spawn.getDespawnedEnemies();
                        while (entityCount-- > 0)
                            spawn.spawn(mob);
                    }

                    // Make sure enough time has passed before the spawn is updated
                    if (System.currentTimeMillis() - spawn.getLastSpawn() < spawn.getSpawnInterval() * 1000) return;

                    // Spawn the mobs, so we reset the spawn counter.
                    spawn.markInterval();

                    int entityCount = spawn.getSpawnCount() - spawn.getEntities().size();
                    while (entityCount-- > 0 && rand.nextInt(100) < spawn.getSpawnChance())
                        spawn.spawn(mob);
                });

        if (block == 0) {
            if (repopulated > 0) {
                Mobs.getInstance().getLogger().info("Repopulated " + repopulated + " spawns.");
                repopulated = 0;
            }

            if (unloaded > 0) {
                Mobs.getInstance().getLogger().info("Cleared " + unloaded + " spawns due to unloaded chunks.");
                unloaded = 0;
            }

            if (distance > 0) {
                Mobs.getInstance().getLogger().info("Cleared " + distance + " spawns due to player distance.");
                distance = 0;
            }
        }
    }
}