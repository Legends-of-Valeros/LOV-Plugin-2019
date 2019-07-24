package com.legendsofvaleros.modules.combatengine.listener;

import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.config.RespawnConfig;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.util.LoggingOut;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Defines CombatEngine's behavior for player respawns.
 */
public class RespawnListener implements Listener {

    private final RespawnConfig config;
    private final Set<UUID> dying;

    public RespawnListener(RespawnConfig config) {
        this.config = config;
        this.dying = new HashSet<>();

        CombatEngine.getInstance().registerEvents(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getBlock().equals(event.getFrom().getBlock())) return;
        if (!dying.contains(event.getPlayer().getUniqueId())) return;

        event.getPlayer().teleport(event.getFrom());
    }

    // if the player logs out while frozen, run the death function immediately
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!dying.contains(event.getPlayer().getUniqueId())) return;

        onDead(event.getPlayer());
    }

    // makes players respawn immediately upon dying. restores a percentage of regenerating stats as
    // they respawn.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        CombatEntity ce = CombatEngine.getEntity(event.getEntity());
        if (ce != null && !ce.isPlayer()) {
            event.setDeathMessage(null);
            event.setDroppedExp(0);
            event.setKeepInventory(true);
            return;
        }

        final Player player = event.getEntity();

        dying.add(player.getUniqueId());

        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.setKeepLevel(true);
        event.setKeepInventory(true);

        // giving a player health during the death event is what causes them to come back to life
        // and avoid the death screen
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * config.getRespawnHealthPercentage());

        player.setGameMode(GameMode.SPECTATOR);
        player.setFlying(true);

        Runnable respawnTask = () -> onDead(event.getEntity());

        if (!LoggingOut.isLoggingOut(player.getUniqueId())) {
            CombatEngine.getInstance().getScheduler().executeInSpigotCircleLater(respawnTask, 5L * 20L);

        } else {
            // special case: player death on logout. Does everything immediately without delays in order
            // to have it complete before the logout process completes.
            monitorDeath(event);
            respawnTask.run();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorDeath(PlayerDeathEvent event) {
        // restores regenerating stats
        CombatEntity ce = CombatEngine.getEntity(event.getEntity());
        if (ce != null && ce.isPlayer()) {
            EntityStats stats = ce.getStats();

            stats.setRegeneratingStat(RegeneratingStat.HEALTH,
                    stats.getStat(Stat.MAX_HEALTH) * config.getRespawnHealthPercentage());

            stats.setRegeneratingStat(RegeneratingStat.MANA,
                    stats.getStat(Stat.MAX_MANA) * config.getRespawnManaPercentage());
            stats.setRegeneratingStat(RegeneratingStat.ENERGY,
                    stats.getStat(Stat.MAX_ENERGY) * config.getRespawnEnergyPercentage());
        }
    }

    // Stops any damage during fake respawn.
    @EventHandler(ignoreCancelled = true)
    public void onCombatEngineDamage(CombatEngineDamageEvent event) {
        if (event.getDamaged().getLivingEntity().getType() == EntityType.PLAYER
                && dying.contains(event.getDamaged().getLivingEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }


    private void onDead(Player player) {
        if (!dying.contains(player.getUniqueId())){
            return;
        }

        // cleanup
        dying.remove(player.getUniqueId());
        player.setGameMode(Bukkit.getDefaultGameMode());
        player.setFlying(false);
        player.setFireTicks(0);

        // mimics normal bukkit behavior
        PlayerRespawnEvent fakeRespawn =
                new PlayerRespawnEvent(player, Bukkit.getWorlds().get(0).getSpawnLocation(), false);
        Bukkit.getPluginManager().callEvent(fakeRespawn);

        // respawns the player
        Location respawnPoint = fakeRespawn.getRespawnLocation();
        if (respawnPoint == null) {
            respawnPoint = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
        }

        player.teleport(respawnPoint);
    }
}
