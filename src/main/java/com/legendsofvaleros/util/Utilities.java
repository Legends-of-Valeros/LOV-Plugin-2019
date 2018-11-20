package com.legendsofvaleros.util;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.util.MessageUtil.ExceptionManager;
import com.legendsofvaleros.util.event.ToggleOpEvent;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utilities extends ListenerModule {
    private static Utilities instance;
    public static Utilities getInstance() {
        return instance;
    }

    private static Set<UUID> op = new HashSet<>();
    public static boolean isOp(Player p) {
        return op.contains(p.getUniqueId());
    }

    /*public static Executor syncExecutor() {
        //return BukkitExecutors.newSynchronous(LegendsOfValeros.getInstance());
        return instance.getScheduler()::sync;
    }
    public static Executor asyncExecutor() {
        //return MoreExecutors.directExecutor();
        return instance.getScheduler()::async;
    }*/

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new LOVCommands());
        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new DebugFlags());

        Discord.onEnable();

        LoggingOut.onEnable();
        ExceptionManager.onEnable(LegendsOfValeros.getInstance().getConfig().getString("dbpools-database"));
        PlayerData.onEnable();

        new TitleUtil();

        Model.onEnable();
        MessageUtil.onEnable();
        Advancements.onEnable();
    }

    public static boolean toggleOp(Player p) {
        if (isOp(p))
            op.remove(p.getUniqueId());
        else
            op.add(p.getUniqueId());
        Bukkit.getPluginManager().callEvent(new ToggleOpEvent(p, isOp(p)));
        return op.contains(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeaveServer(PlayerQuitEvent event) {
        op.remove(event.getPlayer().getUniqueId());
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
                    if (!Utilities.isOp(event.getPlayer()))
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
