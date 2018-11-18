package com.legendsofvaleros.modules.combatengine.core;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineAttackMissEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineSpellDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineTrueDamageEvent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.legendsofvaleros.util.Utilities;

public class AttackForEffectsListener implements Listener {
    public AttackForEffectsListener() {
        LegendsOfValeros.getInstance().getServer().getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackSpell(CombatEngineSpellDamageEvent event) {
        if (event.isCancelled()) return;

        Utilities.spawnParticle(event.getDamaged().getLivingEntity().getLocation(), Particle.DAMAGE_INDICATOR, 3);
        if (event.isCriticalHit())
            Utilities.spawnParticle(event.getDamaged().getLivingEntity().getLocation(), Particle.CRIT_MAGIC, 4);

        if (event.getAttacker() != null && event.getAttacker().isPlayer() &&
                event.getSpellType() != null && event.getSpellType().getHitSound() != null)
            Utilities.playSound((Player) event.getAttacker().getLivingEntity(), event.getSpellType().getHitSound(), 1F);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackPhysical(CombatEnginePhysicalDamageEvent event) {
        if (event.isCancelled()) return;

        Utilities.spawnParticle(event.getDamaged().getLivingEntity().getLocation(), Particle.DAMAGE_INDICATOR, 3);
        if (event.isCriticalHit())
            Utilities.spawnParticle(event.getDamaged().getLivingEntity().getLocation(), Particle.CRIT, 4);

        if (event.getAttacker().isPlayer())
            Utilities.playSound((Player) event.getAttacker().getLivingEntity(), event.getPhysicalAttackType().getHitSound(), 1F);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackTrue(CombatEngineTrueDamageEvent event) {
        if (event.isCancelled()) return;

        if (event.getAttacker() == null) return;

        Utilities.spawnParticle(event.getDamaged().getLivingEntity().getLocation(), Particle.DAMAGE_INDICATOR, 3);

        if (event.getAttacker().isPlayer())
            Utilities.playSound((Player) event.getAttacker().getLivingEntity(), Sound.BLOCK_NOTE_HARP, 1F);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackMiss(CombatEngineAttackMissEvent event) {
        if (event.getAttacker() == null) return;

        if (event.getAttacker().isPlayer())
            Utilities.playSound((Player) event.getAttacker().getLivingEntity(), Sound.BLOCK_NOTE_BASEDRUM, 1F);
    }
}