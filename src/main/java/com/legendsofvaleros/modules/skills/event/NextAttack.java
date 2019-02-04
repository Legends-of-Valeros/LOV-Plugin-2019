package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;
import com.legendsofvaleros.modules.skills.SkillsController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class NextAttack implements Listener {
    private static final HashMap<UUID, NextAttackListener> nextAttack = new HashMap<>();

    public static void on(final UUID uuid, long maxLife, final NextAttackListener listener) {
        nextAttack.put(uuid, listener);

        SkillsController.getInstance().getScheduler().executeInSpigotCircleLater(() -> nextAttack.remove(uuid, listener), maxLife);
    }

    public NextAttack() {
        SkillsController.getInstance().registerEvents(this);
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