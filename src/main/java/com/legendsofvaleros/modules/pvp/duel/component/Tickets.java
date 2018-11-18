package com.legendsofvaleros.modules.pvp.duel.component;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tickets extends DuelComponent {

    private int startingTickets;
    private Map<DuelTeam, Integer> teamTickets;

    public Tickets(int startingTickets) {
        this.startingTickets = startingTickets;
        this.teamTickets = new HashMap<>();
    }

    @Override
    public void handleDeath(Duel duel, CombatEngineDeathEvent event) {
        for (DuelTeam team : duel.getDuelTeams()) {
            if(!team.getTeamMembers().contains(Characters.getPlayerCharacter(event.getDied().getUniqueId()))) continue;

            if(!teamTickets.containsKey(team))
                teamTickets.put(team, startingTickets);

            teamTickets.put(team, teamTickets.get(team) - 1);
        }

        checkForVictory(duel);
    }

    private void checkForVictory(Duel duel) {
        List<DuelTeam> competingTeams = new ArrayList<>();

        for (DuelTeam team : duel.getDuelTeams()) {
            if(teamTickets.get(team) > 0)
                competingTeams.add(team);
        }

        if(competingTeams.size() != 1) return;

        duel.victoryFor(competingTeams.get(0));
    }
}
