package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.util.MessageUtil.ExceptionManager;
import com.legendsofvaleros.util.commands.LOVCommands;
import com.legendsofvaleros.util.commands.TemporaryCommand;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.util.title.TitleUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

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

        if(LegendsOfValeros.getMode().isLenient()) {
            LegendsOfValeros.getInstance().getCommandManager().registerCommand(new LOVCommands());
            LegendsOfValeros.getInstance().getCommandManager().registerCommand(new DebugFlags());
        }

        Discord.onEnable();

        LoggingOut.onEnable();
        ExceptionManager.onEnable(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"));
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
        }else if (e.getMessage().startsWith("/stop")) {
            /*e.setCancelled(true);

            isShutdown = true;

            for(Player p : Bukkit.getOnlinePlayers())
                p.kickPlayer("Server shutting down...");

            // Does this create a race condition with schedulers?
            getScheduler().executeInMyCircleTimer(() -> {
                if(Bukkit.getOnlinePlayers().size() == 0)
                    Bukkit.shutdown();
            }, 0L, 20L);*/
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginServer(PlayerLoginEvent event) {
        if(isShutdown)
            event.setKickMessage("Server shutting down...");
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
        if(LegendsOfValeros.getMode().allowEditing()) return;

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
