package com.legendsofvaleros.modules.keepoutofocean;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineRegenEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.StatUtils;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DependsOn(CombatEngine.class)
public class KeepOutOfOcean extends ModuleListener {

    private long checkTicks;
    private long warningTicks;
    private String warningMessage;
    private long damageTicks;
    private double heartsDamage;

    private Map<UUID, OutOfBoundsPlayer> violating;

    @Override
    public void onLoad() {
        super.onLoad();

        violating = new HashMap<>();

        FileConfiguration config = getConfig();
        checkTicks = config.getLong("check-interval");
        warningTicks = config.getLong("warning-ticks");
        warningMessage = config.getString("warning-message").replace("&", "§");
        damageTicks = config.getLong("ticks-between-damage");
        heartsDamage = config.getDouble("damage");

        new CheckTask(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        violating.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntityType() == EntityType.PLAYER
                && violating.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombatEngineRegen(CombatEngineRegenEvent event) {
        if (event.getRegenerating() == RegeneratingStat.HEALTH
                && violating.containsKey(event.getEntity().getUniqueId())) {
            event.setRegenerationAmount(0.0);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        OutOfBoundsPlayer oobp = violating.get(event.getEntity().getUniqueId());
        if (oobp != null) {
            oobp.finish(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Bukkit.getServer().getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> {
            if (event.getPlayer().isOnline() && isInOcean(event.getPlayer())) {
                double ceDamage = 2 * StatUtils.convertHealth(event.getPlayer(), heartsDamage, false);
                CombatEngine.getInstance().causeTrueDamage(event.getPlayer(), null, ceDamage, null);
            }
        }, 2L);
    }

    private boolean isInOcean(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockZ());
        return biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN;
    }

    /**
     * Checks whether players are in the ocean.
     */
    private class CheckTask extends BukkitRunnable {

        private KeepOutOfOcean plugin;

        private CheckTask(KeepOutOfOcean plugin) {
            this.plugin = plugin;
            runTaskTimer(LegendsOfValeros.getInstance(), checkTicks, checkTicks);
        }

        @Override
        public void run() {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (!violating.containsKey(player.getUniqueId()) && isInOcean(player)) {
                    violating.put(player.getUniqueId(), new OutOfBoundsPlayer(plugin, player));
                }
            }
        }
    }

    /**
     * A player that is currently out of bounds and needs to be encouraged to go back.
     */
    private class OutOfBoundsPlayer extends BukkitRunnable {

        private final Player player;
        private double damageDone;
        private BukkitRunnable warningTask;

        private OutOfBoundsPlayer(final KeepOutOfOcean plugin, Player player) {
            this.player = player;

            MessageUtil.sendError(player, warningMessage);

            final OutOfBoundsPlayer thisClass = this;
            warningTask = new BukkitRunnable() {
                @Override
                public void run() {
                    thisClass.runTaskTimer(LegendsOfValeros.getInstance(), 0, damageTicks);
                }
            };
            warningTask.runTaskLater(LegendsOfValeros.getInstance(), warningTicks);
        }

        @Override
        public void run() {
            if (!isInOcean(player)) {
                finish(false);
                return;
            }
            double ceDamage = StatUtils.convertHealth(player, heartsDamage, false);
            CombatEngine.getInstance().causeTrueDamage(player, player, ceDamage, null);
            damageDone += ceDamage;
        }

        private void finish(boolean died) {
            warningTask.cancel();
            cancel();

            if (!died && damageDone > 0) {
                CombatEntity ce = CombatEngine.getEntity(player);
                if (ce != null) {
                    ce.getStats().editRegeneratingStat(RegeneratingStat.HEALTH, damageDone);
                }
            }

            violating.remove(player.getUniqueId());
        }
    }

}
