package com.legendsofvaleros.modules.parties.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PvPIntegration extends Integration implements Listener {
    public PvPIntegration() {
        PartiesController.getInstance().registerEvents(this);
    }

    @EventHandler
    public void isPvPAllowed(PvPCheckEvent event) {
        // If PvP is already disabled, parties certainly don't need to enable them!
        if(event.isCancelled()) return;

        if(event.getSkill() != null) {
            // Only disable targetting of bad effects in parties.
            if(event.getSkill().getType() != Skill.Type.HARMFUL) return;
        }

        PlayerParty party = PartiesController.getInstance().getPartyByMember(Characters.getPlayerCharacter(event.getAttacker()).getUniqueCharacterId());
        if (party != null && party.getMembers().contains(Characters.getPlayerCharacter(event.getDamaged()).getUniqueCharacterId()))
            event.setCancelled(true);
    }
}
