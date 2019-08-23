package com.legendsofvaleros.modules.arena;

import com.legendsofvaleros.modules.arena.arenamodes.ArenaMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

/**
 * Created by Crystall on 07/24/2019
 */
public class Arena {
    private ArenaMode arenaMode;
    private List<Player> team1;
    private List<Player> team2;
    private boolean isRanked;

    public Arena(List<Player> team1, List<Player> team2, ArenaMode arenaMode, boolean isRanked) {
        this.team1 = team1;
        this.team2 = team2;
        this.arenaMode = arenaMode;
        this.isRanked = isRanked;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        //TODO
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {

    }

    public void onFinish(Player winner) {
        arenaMode.onFinish(winner);
    }

    public List<Player> getTeam1() {
        return team1;
    }

    public List<Player> getTeam2() {
        return team2;
    }

    public ArenaMode getMode() {
        return arenaMode;
    }

}
