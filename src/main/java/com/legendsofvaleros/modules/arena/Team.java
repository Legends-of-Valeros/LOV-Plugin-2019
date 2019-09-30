package com.legendsofvaleros.modules.arena;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Crystall on 08/30/2019
 */
public class Team {
    private List<Player> players = new ArrayList<>();
    private List<Player> alive = new ArrayList<>();

    private String displayName;

    Team(List<Player> players, String displayName) {
        Objects.requireNonNull(players, "Can't init empty team");

        this.players.addAll(players);
        this.alive.addAll(players);
        this.displayName = displayName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getAlive() {
        return alive;
    }

    public String getDisplayName() {
        return displayName;
    }
}