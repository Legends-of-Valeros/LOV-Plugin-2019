package com.legendsofvaleros.modules.pvp.duel.component;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gives players lives.
 */
public class Lives extends DuelComponent {

    private Map<UUID, Integer> lives;
    private int startingLives;
    private int maxLives;

    /**
     * Gives players lives.
     * @param startingLives The amount of lives players start with. This will also be the maximum amount.
     */
    public Lives(int startingLives) {
        this.startingLives = startingLives;
        this.maxLives = startingLives;

        lives = new HashMap<>();
    }

    /**
     * Gives players lives.
     * @param startingLives The amount of lives players start with. This will also be the maximum amount.
     * @param maxLives The maximum amount of lives players can have.
     */
    public Lives(int startingLives, int maxLives) {
        this.startingLives = startingLives;
        this.maxLives = maxLives;

        lives = new HashMap<>();
    }

    public void setLives(UUID uuid, int lives) {
        this.lives.put(uuid, lives > maxLives ? maxLives : lives);
    }

    public int getLives(UUID uuid) {
        return this.lives.get(uuid);
    }

    public void addLives(UUID uuid, int lives) {
        int newLives = this.lives.get(uuid) + lives;
        this.lives.put(uuid, (newLives > maxLives ? maxLives : newLives));
    }

    @Override
    public void handleDeath(Duel duel, CombatEngineDeathEvent event) {
        CombatEntity died = event.getDied();

        if(!lives.containsKey(died.getUniqueId()))
            lives.put(died.getUniqueId(), startingLives);

        lives.put(died.getUniqueId(), lives.get(died.getUniqueId()) - 1);

        checkForVictory(duel);
    }

    private void checkForVictory(Duel duel) {
        List<DuelTeam> contestingTeams = duel.getDuelTeams().stream().filter(team -> {
            int totalLives = 0;

            for (PlayerCharacter playerCharacter : team.getTeamMembers()) {
                totalLives += lives.get(playerCharacter.getPlayerId());
            }

            return totalLives > 0;
        }).collect(Collectors.toList());

        if(contestingTeams.size() == 1) {
            DuelTeam victors = contestingTeams.get(0);

            duel.victoryFor(victors);
        }
    }
}
