package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class NextAttack implements Listener {
    private static final HashMap<UUID, NextAttackListener> nextAttack = new HashMap<>();

    public static void on(final UUID uuid, long maxLife, final NextAttackListener listener) {
        nextAttack.put(uuid, listener);

        Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> nextAttack.remove(uuid, listener), maxLife);
    }

    public NextAttack(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerLogout(PlayerCharacterLogoutEvent event) {
        if (!nextAttack.containsKey(event.getPlayer().getUniqueId()))
            return;

        nextAttack.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(CombatEnginePhysicalDamageEvent event) {
        if (!nextAttack.containsKey(event.getAttacker().getUniqueId())) return;

        nextAttack.remove(event.getAttacker().getUniqueId()).run(event);
    }
}