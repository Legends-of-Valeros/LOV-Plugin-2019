package com.legendsofvaleros.modules.mobs.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.mobs.MobsController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class MobSpawner {
    private final Random rand = new Random();

    private final int allUpdateInterval;

    private int repopulated = 0;
    private int unloaded = 0;
    private int distance = 0;

    public MobSpawner() {
        this.allUpdateInterval = MobsController.getInstance().getConfig().getInt("spawn-area-update-smear", 20 * 10);

        MobsController.getInstance().getLogger().info("Smearing spawn updates across " + allUpdateInterval + " ticks.");

        new BukkitRunnable() {
            private long time = 0;

            @Override
            public void run() {
                updateSpawn(time++);
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 1, 1);
    }

    private void updateSpawn(long time) {
        Collection<SpawnArea> loaded = MobsController.getInstance().getLoadedSpawns();

        int block = (int) (time % allUpdateInterval);
        int blockSize = (int) Math.ceil((double) loaded.size() / allUpdateInterval);

        loaded.stream()
                .skip(block * blockSize).limit(blockSize)
                .forEach(spawn -> {
                    spawn.updateStats();

                    World world = spawn.getWorld();
                    Location spawnLocation = spawn.getLocation();

                    if (!spawnLocation.getChunk().isLoaded()) {
                        if (spawn.getEntities().size() > 0) {
                            MobsController.getInstance().getLogger().info("Clearing spawn due to unloaded chunk.");
                            unloaded++;

                            spawn.clear();
                        }

                        spawn.setDebugInfo("Unloaded");

                        return;
                    }

                    boolean playerNearby = false;

                    List<org.bukkit.entity.Entity> entities = new ArrayList<>(
                            world.getNearbyEntities(spawnLocation,
                                    spawn.getRadius() + 25,
                                    spawn.getRadius() + 20,
                                    spawn.getRadius() + 25));
                    CombatEntity ce;
                    for (org.bukkit.entity.Entity e : entities) {
                        if (!(e instanceof Player)) continue;
                        ce = CombatEngine.getEntity((Player) e);
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

                        spawn.setDebugInfo("No players nearby");
                    } else {
                        Mob mob = spawn.getMob();
                        if (mob == null) {
                            Bukkit.broadcastMessage(ChatColor.RED + "[!] Unknown instance ID. Offender: " + spawn.getEntityId() + " at " + spawn.getLocation());
                            return;
                        }

                        if (spawn.getDespawnedEnemies() > 0) {
                            repopulated++;

                            spawn.repopulated();

                            int entityCount = spawn.getDespawnedEnemies();
                            while (entityCount-- > 0) {
                                spawn.spawn(mob);
                            }
                        }

                        // Make sure enough time has passed before the spawn is updated
                        if (System.currentTimeMillis() - spawn.getLastInterval() < spawn.getInterval() * 1000) {
                            spawn.setDebugInfo("Waiting for timeout");
                            return;
                        }

                        // Spawn the mobs, so we reset the spawn counter.
                        spawn.markInterval();

                        int entityCount = spawn.getCount() - spawn.getEntities().size();
                        while (entityCount-- > 0 && rand.nextInt(100) < spawn.getChance())
                            spawn.spawn(mob);

                        spawn.setDebugInfo("No problems");
                    }

                    spawn.updateStats();
                });

        if (block == 0) {
            if (repopulated > 0) {
                MobsController.getInstance().getLogger().info("Repopulated " + repopulated + " spawns.");
                repopulated = 0;
            }

            if (unloaded > 0) {
                MobsController.getInstance().getLogger().info("Cleared " + unloaded + " spawns due to unloaded chunks.");
                unloaded = 0;
            }

            if (distance > 0) {
                MobsController.getInstance().getLogger().info("Cleared " + distance + " spawns due to player distance.");
                distance = 0;
            }
        }
    }
}