package com.legendsofvaleros.modules.pvp.listener;

import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skill.SkillTargetEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PvPListener implements Listener {
    private PvPController pvp = PvPController.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(CombatEngineDamageEvent event) {
        if(event.isCancelled()) return;

        if(event.getAttacker() == null) return;

        if (!event.getAttacker().isPlayer() || !event.getDamaged().isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded((Player)event.getDamaged().getLivingEntity())) return;
        if (!Characters.isPlayerCharacterLoaded((Player)event.getAttacker().getLivingEntity())) return;

        // If PvP is disabled, cancel it. Duh.
        if(!pvp.isPvPEnabled()) { event.setCancelled(true); }

        // We still need to check if PvP is allowed. (For duels and such)
        PvPCheckEvent pvp = new PvPCheckEvent((Player)event.getAttacker().getLivingEntity(), (Player)event.getDamaged().getLivingEntity(), null);
        Bukkit.getPluginManager().callEvent(pvp);

        if(pvp.isCancelled())
            event.setCancelled(true);
        else{
            // If the damage event is not cancelled, add the PvP modifier.
            event.newDamageModifierBuilder("PvP")
                    .setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
                    .setValue(PvPController.DAMAGE_MULTIPLIER)
                    .build();
        }

        /*if(!attackerToggle.isEnabled() || !targetToggle.isEnabled() || attackerToggle.getPriority() != targetToggle.getPriority()) {
            event.setCancelled(true);
            return;
        }*/
    }

    @EventHandler
    public void onEntityTargetted(SkillTargetEvent event) {
        // Ignore "good" spells. We only care about harmful attacks.
        if(event.getSkill().getType() != Skill.Type.HARMFUL)
            return;

        if (!event.getUser().isPlayer() || !event.getTarget().isPlayer()) return;

        if (!Characters.isPlayerCharacterLoaded((Player)event.getUser().getLivingEntity())) return;
        if (!Characters.isPlayerCharacterLoaded((Player)event.getTarget().getLivingEntity())) return;

        // If PvP is disabled, cancel it. Duh.
        if(!pvp.isPvPEnabled()) { event.setCancelled(true); }

        // We still need to check if PvP is allowed. (For duels and such)
        PvPCheckEvent pvp = new PvPCheckEvent((Player)event.getUser().getLivingEntity(), (Player)event.getTarget().getLivingEntity(), event.getSkill());
        Bukkit.getPluginManager().callEvent(pvp);

        // PvP is disabled! Don't target the player!
        if(pvp.isCancelled())
            event.setCancelled(true);
    }
}
