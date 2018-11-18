package com.legendsofvaleros.modules.pvp.duel.component;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import org.bukkit.entity.LivingEntity;

/**
 * Prevents a player from dying in this duel.
 */
public class DeathPrevention extends DuelComponent {

    private double preventionHealth;

    /**
     * Prevents a player from dying in this duel.
     * @param preventionHealth The health to heal the player to when they "die".
     */
    public DeathPrevention(double preventionHealth) {
        this.preventionHealth = preventionHealth;
    }

    @Override
    public void handleDamage(Duel duel, CombatEngineDamageEvent event) {
        CombatEntity target = event.getDamaged();
        LivingEntity entity = target.getLivingEntity();

        if (!(entity.getHealth() - event.getFinalDamage() < 0)) return;
        entity.setHealth(preventionHealth);
        event.setCancelled(true);
    }
}
