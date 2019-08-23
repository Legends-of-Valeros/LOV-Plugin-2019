package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.commands.LOVCommands;
import com.legendsofvaleros.util.commands.TemporaryCommand;
import com.legendsofvaleros.util.model.Model;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Date;

@ModuleInfo(name = "Utilities", info = "")
public class Utilities extends ListenerModule {
    private static Utilities instance;

    public static Utilities getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new TemporaryCommand());

        if (LegendsOfValeros.getMode().isLenient()) {
            LegendsOfValeros.getInstance().getCommandManager().registerCommand(new LOVCommands());
            LegendsOfValeros.getInstance().getCommandManager().registerCommand(new DebugFlags());
        }

        Discord.onEnable();
        LoggingOut.onEnable();

        new TitleUtil();

        Model.onLoad();
        MessageUtil.onEnable();
        Advancements.onEnable();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        Model.onPostLoad();

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandProcess(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().startsWith("/reload")) {
            e.setCancelled(true);

            MessageUtil.sendError(e.getPlayer(), "*smacks you with a newspaper* Don't do that.");
        } else if (e.getMessage().startsWith("/stop")) {
            e.setCancelled(true);

            shutdown(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandProcess(ServerCommandEvent e) {
        if (e.getCommand().startsWith("reload")) {
            e.setCancelled(true);

            MessageUtil.sendError(e.getSender(), "*smacks you with a newspaper* Don't do that.");
        } else if (e.getCommand().startsWith("stop")) {
            e.setCancelled(true);

            shutdown(e.getSender());
        }
    }

    private void shutdown(CommandSender sender) {
        if (LegendsOfValeros.getInstance().isShutdown()) {
            MessageUtil.sendError(sender, "The server is already shutting down!");
            return;
        }

        LegendsOfValeros.getInstance().setShutdown(true);

        for (Player p : Bukkit.getOnlinePlayers())
            try {
                p.kickPlayer("Legends of Valeros is restarting.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        // Not sure if this actually helps, but wait until the next tick
        // to start additional cleanup. This allows event handlers to
        // finish (maybe)? Does this create a race condition?
        getScheduler().executeInMyCircleTimer(() -> {
            for (InternalScheduler scheduler : InternalScheduler.getAllSchedulers()) {
                if (scheduler == getScheduler()) continue;

                scheduler.shutdown();

                if (scheduler.getTasksRemaining() > 0) {
                    getLogger().info("Waiting for " + scheduler.getTasksRemaining() + " tasks to complete in " + scheduler.getName() + " (" + scheduler.getCurrentTick() + ")...");
                    for (InternalTask task : scheduler.getTasksQueued())
                        getLogger().info(" -" + task.toString());
                    return;
                }
            }

            // Once all other schedulers have shut down, start the shutdown of my scheduler
            getScheduler().shutdown();

            if (getScheduler().getTasksRemaining() > 0) {
                getLogger().info("Waiting for " + getScheduler().getTasksRemaining() + " tasks to complete...");
            } else {
                Bukkit.shutdown();
            }
        }, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginServer(AsyncPlayerPreLoginEvent event) {
        if (LegendsOfValeros.getInstance().isShutdown()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Legends of Valeros is shutting down...");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeaveServer(PlayerQuitEvent event) {
        DebugFlags.debug.remove(event.getPlayer().getUniqueId());
    }

    public static void playSound(Location loc, Sound sound, float pitch) {
        loc.getWorld().playSound(loc, sound, pitch, 1F);
    }

    public static void playSound(Player p, Sound sound, float pitch) {
        p.playSound(p.getLocation(), sound, pitch, 1F);
    }

    public static void spawnParticle(Location loc, Particle particle, int data) {
        if (loc == null) {
            MessageUtil.sendError(Bukkit.getConsoleSender(), "Attempt to spawn particle at null location.");
            return;
        }

        loc.getWorld().spawnParticle(particle, loc, data);
    }

    /**
     * Checks whether {@code entity} is exposed to sunlight.
     * @param entity entity to check
     * @return true if {@code entity} is exposed to sunlight, otherwise false
     */
    public boolean exposedToSunlight(Entity entity) {
        Vector vector = entity.getLocation().toVector();
        // When using 256 it loops back to 0.
        int distance = 255 - vector.getBlockY();
        BlockIterator it = new BlockIterator(entity.getWorld(), vector, new Vector(0, 1, 0), 0, distance);
        while (it.hasNext()) {
            if (it.next().getType().isSolid())
                return false;
        }
        return true;
    }


    /**
     * Returns the current uptime of the server
     * @return
     */
    public static String getUptime() {
        Date d = new Date(System.currentTimeMillis() - LegendsOfValeros.startTime);
        String rest = "";
        if (d.getHours() > 10) rest += "" + (d.getHours() - 1);
        else rest += "0" + (d.getHours() - 1);
        rest += ":";
        if (d.getMinutes() > 9) rest += "" + (d.getMinutes());
        else rest += "0" + (d.getMinutes());
        rest += ":";
        if (d.getSeconds() > 9) rest += "" + (d.getSeconds());
        else rest += "0" + (d.getSeconds());
        return (d.getDay() > 4 ? (d.getDay() - 4) + "" + ChatColor.GREEN + " Days " + ChatColor.GRAY : "") + rest;
    }

    /**
     * Remove a potion with infinite duration.
     * @param player
     * @param types
     */
    public static void removeInfinitePotion(Player player, PotionEffectType... types) {
        for (PotionEffectType type : types) {
            PotionEffect pe = player.getPotionEffect(type);
            if (pe != null && pe.getDuration() >= 30 * 60 * 20)
                player.removePotionEffect(type);
        }
    }

}
