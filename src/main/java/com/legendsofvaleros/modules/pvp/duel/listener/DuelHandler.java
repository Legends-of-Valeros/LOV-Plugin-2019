package com.legendsofvaleros.modules.pvp.duel.listener;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.pvp.duel.DuelManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DuelHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(CombatEngineDamageEvent event) {
        if(event.isCancelled()) return;

        CombatEntity attacker = event.getAttacker();
        CombatEntity target = event.getDamaged();

        if (!attacker.isPlayer() || !target.isPlayer()) return;

        Duel attackerDuel = DuelManager.getDuelFor(attacker.getUniqueId());
        Duel targetDuel = DuelManager.getDuelFor(target.getUniqueId());

        if (attackerDuel != targetDuel || attackerDuel == null || targetDuel == null) {
            event.setCancelled(true);
            return;
        }

        attackerDuel.handleDamage(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(CombatEngineDeathEvent event) {
        CombatEntity target = event.getDied();

        if (!target.isPlayer()) return;

        Duel targetDuel = DuelManager.getDuelFor(target.getUniqueId());

        if(targetDuel != null)
            targetDuel.handleDeath(event);
    }

}
