package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.skills.EntitiesListener;
import com.legendsofvaleros.modules.skills.Skills;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OnProjectile implements Listener {
    private static final HashMap<UUID, EntitiesListener> onProjectile = new HashMap<>();

    public static <T extends Projectile> T shoot(CombatEntity spawner, double speed, int maxDistance, Class<T> projectile, EntitiesListener listener) {
        final T p = spawner.getLivingEntity().launchProjectile(projectile);
        p.setVelocity(spawner.getLivingEntity().getEyeLocation().getDirection().multiply(speed));
        return shoot(p, maxDistance, listener);
    }

    public static <T extends Projectile> T shoot(CombatEntity spawner, Location loc, int maxDistance, Class<T> projectile, EntitiesListener listener) {
        return shoot(spawner, loc, null, maxDistance, projectile, listener);
    }

    public static <T extends Projectile> T shoot(CombatEntity spawner, Location loc, double speed, int maxDistance, Class<T> projectile, EntitiesListener listener) {
        return shoot(spawner, loc, loc.getDirection().multiply(speed), maxDistance, projectile, listener);
    }

    public static <T extends Projectile> T shoot(CombatEntity spawner, Location loc, Vector velocity, int maxDistance, Class<T> projectile, EntitiesListener listener) {
        final T p = loc.getWorld().spawn(loc, projectile);
        p.setShooter(spawner.getLivingEntity());
        if (velocity != null)
            p.setVelocity(velocity);
        return shoot(p, maxDistance, listener);
    }

    private static <T extends Projectile> T shoot(T p, int maxDistance, EntitiesListener listener) {
        onProjectile.put(p.getUniqueId(), listener);

        p.setSilent(true);
        p.setInvulnerable(true);
        p.setGravity(false);

        final Location start = p.getLocation().clone();
        new BukkitRunnable() {
            public void run() {
                if (p.isDead()) {
                    cancel();
                    return;
                }

                if (!onProjectile.containsKey(p.getUniqueId()) || p.getLocation().distance(start) >= maxDistance) {
                    onProjectile.remove(p.getUniqueId());
                    p.remove();
                    cancel();
                }
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), 1L, 2L);

        return p;
    }

    public OnProjectile() {
        Skills.getInstance().registerEvents(this);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (!onProjectile.containsKey(entity.getUniqueId()))
            return;

        List<LivingEntity> entities = new ArrayList<>();
        for (Entity e : entity.getNearbyEntities(2, 2, 2)) {
            if (!(e instanceof LivingEntity)) continue;
            if (e == entity.getShooter() || CombatEngine.getEntity((LivingEntity) e) == null) continue;
            entities.add((LivingEntity) e);
        }

        EntitiesListener list = onProjectile.get(entity.getUniqueId());
        if (list != null)
            list.run(CombatEngine.getEntity((LivingEntity) entity.getShooter()), entities);
        onProjectile.remove(entity.getUniqueId());

        event.getEntity().remove();
    }
}