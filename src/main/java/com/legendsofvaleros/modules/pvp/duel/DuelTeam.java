package com.legendsofvaleros.modules.pvp.duel;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelTeam {
    private ChatColor teamColor;
    private List<PlayerCharacter> duelPlayers;

    public DuelTeam(ChatColor teamColor) {
        this.teamColor = teamColor;
        this.duelPlayers = new ArrayList<>();
    }

    public DuelTeam(ChatColor teamColor, PlayerCharacter... players) {
        this.teamColor = teamColor;
        this.duelPlayers = Arrays.asList(players);
    }

    public List<PlayerCharacter> getTeamMembers() {
        return duelPlayers;
    }
}
