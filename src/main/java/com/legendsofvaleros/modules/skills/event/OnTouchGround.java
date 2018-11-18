package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class OnTouchGround implements Listener {
    public static void call(LivingEntity entity, OnTouchGroundListener listener) {
        new BukkitRunnable() {
            public void run() {
                if (entity == null || entity.isDead())
                    cancel();

                if (entity.isOnGround()) {
                    listener.run(entity);
                    cancel();
                }
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 1L, 2L);
    }
}