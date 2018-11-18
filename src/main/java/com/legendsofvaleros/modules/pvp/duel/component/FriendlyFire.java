package com.legendsofvaleros.modules.pvp.duel.component;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Allows or disallows friendly fire.
 */
public class FriendlyFire extends DuelComponent {

    private boolean allowed;

    /**
     * Allows or disallows friendly fire.
     * @param allowed Is friendly fire allowed?
     */
    public FriendlyFire(boolean allowed) {
        this.allowed = allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean getAllowed() {
        return this.allowed;
    }

    @Override
    public void handleDamage(Duel duel, CombatEngineDamageEvent event) {
        if(allowed)
            return;

        PlayerCharacter attacker = Characters.getPlayerCharacter(event.getAttacker().getUniqueId());
        PlayerCharacter target = Characters.getPlayerCharacter(event.getAttacker().getUniqueId());

        List<DuelTeam> teams = duel.getDuelTeams();

        for (DuelTeam team : teams) {
            if(!team.getTeamMembers().containsAll(Arrays.asList(attacker, target)))
                continue;

            event.setCancelled(true);
            break;
        }
    }
}
