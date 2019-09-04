package com.legendsofvaleros.modules.arena;

import com.legendsofvaleros.modules.arena.arenamodes.ArenaMode;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.dueling.core.Duel;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

/**
 * Created by Crystall on 07/24/2019
 */
public class Arena implements Listener {
    private ArenaMode arenaMode;
    private Team team1;
    private Team team2;
    private boolean isRanked;


    public Arena(List<Player> team1, List<Player> team2, ArenaMode arenaMode, boolean isRanked) {
        this.team1 = new Team(team1, "Team 1");
        this.team2 = new Team(team2, "Team 2");

        this.arenaMode = arenaMode;
        this.isRanked = isRanked;

        this.team1.getPlayers().forEach(player -> player.teleport(ArenaController.ARENA_MIDDLE_POSITION));
        this.team2.getPlayers().forEach(player -> player.teleport(ArenaController.ARENA_MIDDLE_POSITION));

        ArenaController.getInstance().registerEvents(this);
    }

    private void handlePlayerDeath(Player player) {
        if (team1.getAlive().contains(player)) {
            team1.getAlive().remove(player);

            if (team1.getAlive().isEmpty()) {
                onFinish(team2);
            }
            return;
        }

        if (team2.getAlive().contains(player)) {
            team2.getAlive().remove(player);

            if (team2.getAlive().isEmpty()) {
                onFinish(team1);
            }
        }
    }

    public void onFinish(Team winner) {
        Title title = new Title("Game end!", winner.getDisplayName() + " won!", 10, 40, 10);
        title.setTimingsToTicks();
        title.setSubtitleColor(ChatColor.GOLD);

        team1.getPlayers().forEach(player -> TitleUtil.queueTitle(title, player));
        team2.getPlayers().forEach(player -> TitleUtil.queueTitle(title, player));

        arenaMode.onFinish(winner);

        if (isRanked) {
            //TODO apply ranked elo change
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void isPvPAllowed(PvPCheckEvent event) {
        Team damaged = getTeamForPlayer(event.getDamaged());
        Team attacker = getTeamForPlayer(event.getAttacker());
        //one of both players is not in the arena fight -> cancel damage
        if (damaged == null || attacker == null) {
            event.setCancelled(true);
            return;
        }

        // cancel damage if they are team members, allow damage otherwise
        event.setCancelled(damaged.equals(attacker));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(CombatEngineDamageEvent event) {
        // Prevent death and end the duel
        if (event.getDamaged().getStats().getRegeneratingStat(RegeneratingStat.HEALTH) - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            d.onDeath((Player) event.getDamaged().getLivingEntity());
        } else {
            d.onDamage(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.handlePlayerDeath(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.handlePlayerDeath(event.getPlayer());
        this.team1.getPlayers().remove(event.getPlayer());
        this.team2.getPlayers().remove(event.getPlayer());
    }

    public Team getTeamForPlayer(Player player) {
        if (team1.getPlayers().contains(player)) {
            return team1;
        }
        if (team2.getPlayers().contains(player)) {
            return team2;
        }
        return null;
    }


    public Team getTeamOne() {
        return team1;
    }

    public Team getTeamTwo() {
        return team2;
    }

    public ArenaMode getMode() {
        return arenaMode;
    }

}
