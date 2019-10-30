package com.legendsofvaleros.modules.pvp.listener;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.classes.skills.events.SkillTargetEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PvPListener implements Listener {
    private PvPController pvp = PvPController.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(CombatEngineDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getAttacker() == null) {
            return;
        }

        if (!event.getAttacker().isPlayer() || !event.getDamaged().isPlayer()) {
            return;
        }

        if (!Characters.isPlayerCharacterLoaded((Player) event.getDamaged().getLivingEntity())) {
            return;
        }
        if (!Characters.isPlayerCharacterLoaded((Player) event.getAttacker().getLivingEntity())) {
            return;
        }

        // If PvP is disabled, cancel it. Duh.
        if (!pvp.isPvPEnabled()) {
            event.setCancelled(true);
        }

        // We still need to check if PvP is allowed. (For duels and such)
        PvPCheckEvent pvpCE = new PvPCheckEvent((Player) event.getAttacker().getLivingEntity(), (Player) event.getDamaged().getLivingEntity(), null);
        Bukkit.getPluginManager().callEvent(pvpCE);

        if (pvpCE.isCancelled()) {
            event.setCancelled(true);
        } else {
            // If the damage event is not cancelled, add the PvP modifier.
            event.newDamageModifierBuilder("PvP")
                    .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                    .setValue(PvPController.DAMAGE_MULTIPLIER)
                    .build();
        }
    }

    @EventHandler
    public void onEntityTargeted(SkillTargetEvent event) {
        // Ignore "good" spells. We only care about harmful attacks.
        if (event.getSkill().getType() != Skill.Type.HARMFUL)
            return;

        if (!event.getUser().isPlayer() || !event.getTarget().isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded((Player) event.getUser().getLivingEntity())) return;
        if (!Characters.isPlayerCharacterLoaded((Player) event.getTarget().getLivingEntity())) return;

        // If PvP is disabled, cancel it. Duh.
        if (!pvp.isPvPEnabled()) {
            event.setCancelled(true);
        }

        // We still need to check if PvP is allowed. (For duels and such)
        PvPCheckEvent pvpCE = new PvPCheckEvent((Player) event.getUser().getLivingEntity(), (Player) event.getTarget().getLivingEntity(), event.getSkill());
        Bukkit.getPluginManager().callEvent(pvpCE);

        // PvP is disabled! Don't target the player!
        if (pvpCE.isCancelled()) {
            event.setCancelled(true);
        }
    }
}
