package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.skills.Skills;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FallDamage implements Listener {
    private static final List<UUID> nextFall = new ArrayList<>();

    public static void cancel(final UUID uuid, final long maxLife) {
        nextFall.add(uuid);

        new BukkitRunnable() {
            public void run() {
                nextFall.remove(uuid);
            }
        }.runTaskLater(LegendsOfValeros.getInstance(), maxLife);
    }

    public FallDamage() {
        Skills.getInstance().registerEvents(this);
    }

    @EventHandler
    public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
        if (!nextFall.contains(event.getPlayer().getUniqueId()))
            return;

        nextFall.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLivingEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != DamageCause.FALL)
            return;
        if (!nextFall.contains(event.getDamager().getUniqueId()))
            return;

        nextFall.remove(event.getDamager().getUniqueId());

        event.setDamage(0);
    }
}