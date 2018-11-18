package com.legendsofvaleros.modules.skills.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ParticleFollow {
    private static final Multimap<UUID, BukkitTask> particleAt = HashMultimap.create();

    public static void follow(final Entity follow, final long maxLife, final Particle p, final int count, final double xOffset, final double yOffset, final double zOffset) {
        follow(follow, maxLife, p, count, xOffset, yOffset, zOffset, 0.01, null);
    }

    public static void follow(final Entity follow, final long maxLife, final Particle p, final int count, final double xOffset, final double yOffset, final double zOffset, final double speed) {
        follow(follow, maxLife, p, count, xOffset, yOffset, zOffset, speed, null);
    }

    public static void follow(final Entity follow, final long maxLife, final Particle p, final int count, final double xOffset, final double yOffset, final double zOffset, final Object data) {
        follow(follow, maxLife, p, count, xOffset, yOffset, zOffset, 0.01, data);
    }

    public static void follow(final Entity follow, final long maxLife, final Particle p, final int count, final double xOffset, final double yOffset, final double zOffset, final double speed, final Object data) {
        UUID uuid = follow.getUniqueId();
        new BukkitRunnable() {
            long timeExisted = 0;

            public void run() {
                if (follow.isDead() || timeExisted >= maxLife) {
                    for (BukkitTask task : particleAt.get(uuid))
                        task.cancel();
                    particleAt.removeAll(uuid);
                    return;
                }

                follow.getWorld().spawnParticle(p, follow.getLocation(), count, xOffset, yOffset, zOffset, speed, data);

                timeExisted++;
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 0L, 1L);
    }
}