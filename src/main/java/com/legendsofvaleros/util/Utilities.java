package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.commands.LOVCommands;
import com.legendsofvaleros.util.commands.TemporaryCommand;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

// TODO: Create subclass for listeners?
@ModuleInfo(name = "Utilities", info = "")
public class Utilities extends ModuleListener {
    private static Utilities instance;

    public static Utilities getInstance() {
        return instance;
    }

    private boolean isShutdown = false;

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

        PlayerData.onEnable();

        new TitleUtil();

        Model.onEnable();
        MessageUtil.onEnable();
        Advancements.onEnable();
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
        if (isShutdown) {
            MessageUtil.sendError(sender, "The server is already shutting down!");
            return;
        }

        isShutdown = true;

        for (Player p : Bukkit.getOnlinePlayers())
            try {
                p.kickPlayer("Server shutting down...");
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
                    for(InternalTask task : scheduler.getTasksQueued())
                        getLogger().info(" -" + task.toString());
                    return;
                }
            }

            // Once all other schedulers have shut down, start the shutdown of my scheduler
            getScheduler().shutdown();

            if (getScheduler().getTasksRemaining() > 0)
                getLogger().info("Waiting for " + getScheduler().getTasksRemaining() + " tasks to complete...");
            else
                Bukkit.shutdown();
        }, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginServer(AsyncPlayerPreLoginEvent event) {
        if (isShutdown) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server is shutting down...");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeaveServer(PlayerQuitEvent event) {
        DebugFlags.debug.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            event.setCancelled(true);
        } else if (event.getRightClicked() instanceof Painting) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch (event.getClickedBlock().getType()) {
                case CHEST:
                case HOPPER:
                case FURNACE:
                case WORKBENCH:
                case ANVIL:
                case ENDER_CHEST:
                case ENCHANTMENT_TABLE:
                case TRAPPED_CHEST:
                case DISPENSER:
                case DROPPER:
                case ITEM_FRAME:
                case BREWING_STAND:
                    event.setCancelled(true);
                default:
                    if (event.getClickedBlock().getType().name().endsWith("_SHULKER_BOX"))
                        event.setCancelled(true);
                    break;
            }
        }

        //prevent farmland from being trampled
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL) {
            event.setCancelled(true);
        }
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

}
