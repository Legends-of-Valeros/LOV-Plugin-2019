package com.legendsofvaleros.modules.restrictions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.ServerMode;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.util.Discord;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;

import java.util.stream.Collectors;

/**
 * Created by Crystall on 04/11/2019
 */
public class RestrictionsController extends ModuleListener {

    /**
     * Removes endless potions in case they have some
     * @param event
     */
    @EventHandler
    public void onEndlessPotionEffect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(t -> Utilities.removeInfinitePotion(player, t));
    }

    /**
     * Semi-prevent world downloader (does not prevent world downloading overall)
     * @param evt
     */
    @EventHandler
    public void onChannelRegister(PlayerRegisterChannelEvent evt) {
        if (!evt.getChannel().equalsIgnoreCase("WDL|INIT")) {
            return;
        }
        evt.getPlayer().kickPlayer(ChatColor.RED + "Please disable World Downloader.");
        Discord.sendLogMessage("**" + evt.getPlayer().getName() + "** was kicked for using World Downloader!");
    }

    /**
     * Warn staff of people that may be using boat fly
     * @param evt
     */
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent evt) {
        String fly = evt.getVehicle().getPassengers().stream().filter(Player.class::isInstance).map(Entity::getName)
                .collect(Collectors.joining(", "));
        if (evt.getVehicle().getType() == EntityType.BOAT && !evt.getFrom().getBlock().isLiquid() && fly.length() > 0
                && evt.getTo().getY() > evt.getFrom().getY() && evt.getVehicle().getVelocity().getY() <= 0) {
            Discord.sendLogMessage("**[Anti-Cheat]** " + ChatColor.GRAY + fly + " may be using BoatFly.");
        }
    }

    /**
     * Send warnings if a staff member is using game modes on the live server
     * @param evt
     */
    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE) && evt.getNewGameMode() == GameMode.CREATIVE) {
            evt.setCancelled(true);
            Discord.sendLogMessage("**" + evt.getPlayer().getName() + "** tried to enter " + evt.getNewGameMode().name().toLowerCase() + "! This should not happen.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preventBucketEmpty(PlayerBucketEmptyEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE)) {
            evt.setCancelled(evt.getBlockClicked().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preventBucketFill(PlayerBucketFillEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE)) {
            evt.setCancelled(evt.getBlockClicked().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinecartDamage(EntityDamageEvent evt) {
        evt.setCancelled(evt.getEntityType().name().startsWith("MINECART"));
    }

    /**
     * Prevent players from using teleport when in spectator mode
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent evt) {
        evt.setCancelled(evt.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE);
    }

    /**
     * Prevent players from leaving the world
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent evt) {
        int max = (int) evt.getTo().getWorld().getWorldBorder().getSize() / 2;
        Location to = evt.getTo();
        if (Math.abs(to.getX()) > max + 3 || Math.abs(to.getZ()) > max + 3) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(ChatColor.RED + "You can't leave the world. Please go back");
        }
    }

    /**
     * Prevent messing with item frames.
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemFrameInteract(PlayerInteractEntityEvent evt) {
        evt.setCancelled(evt.getRightClicked().getType() == EntityType.ITEM_FRAME);
    }

    /**
     * Prevent players from punching items out of item frames.
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingPunch(EntityDamageByEntityEvent evt) {
        evt.setCancelled(evt.getEntity() instanceof Hanging);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingExplode(HangingBreakEvent evt) {
        evt.setCancelled(evt.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION);
        Hanging ent = evt.getEntity();
        if (evt.getCause() == HangingBreakEvent.RemoveCause.PHYSICS && ent.getLocation().getBlock().getRelative(evt.getEntity().getFacing().getOppositeFace()).getType() == Material.AIR)
            ent.remove();
    }

    /**
     * Prevent enderman to teleport around (in case we want to use them as mob entities)
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onEnderTeleport(EntityTeleportEvent evt) {
        evt.setCancelled(evt.getEntityType() == EntityType.ENDERMAN);
    }

    /**
     * Prevent messing around with armor stands (especially invisible ones)
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * Extra check for interaction with prohibited entities
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand ||
                event.getRightClicked() instanceof Painting) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
            if (event.getClickedBlock().getType().name().endsWith("_SHULKER_BOX") ||
                    event.getClickedBlock().getType().name().contains("DOOR")) {
                event.setCancelled(true);
            }
            switch (event.getClickedBlock().getType()) {
                //TODO replace legacy materials
                case LEGACY_SOIL:
                case LEGACY_CROPS:
                case CHEST:
                case HOPPER:
                case FURNACE:
                case LEGACY_WORKBENCH:
                case ANVIL:
                case ENDER_CHEST:
                case LEGACY_ENCHANTMENT_TABLE:
                case TRAPPED_CHEST:
                case DISPENSER:
                case DROPPER:
                case BREWING_STAND:
                case FLOWER_POT:
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @param evt
     */
    @EventHandler
    public void onTNT(EntityExplodeEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent evt) {
        if (evt.getPlayer().getGameMode() != GameMode.CREATIVE) {
            evt.setCancelled(evt.getBlock().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
        }
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE)) {
            evt.setCancelled(evt.getBlock().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
        }
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPortalDestroy(BlockPhysicsEvent evt) {
        //TODO replace legacy materials
        evt.setCancelled(evt.getBlock().getType() == Material.LEGACY_PORTAL && evt.getChangedType() != Material.LEGACY_PORTAL);
    }

    /**
     * @param evt
     */
    @EventHandler
    public void preventSpread(BlockSpreadEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void preventLeafDecay(LeavesDecayEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockIgniteEvent evt) {
        evt.setCancelled(evt.getCause() == BlockIgniteEvent.IgniteCause.SPREAD);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler
    public void onBlockFade(BlockFadeEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onWeather(WeatherChangeEvent evt) {
        if (evt.getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME)) {
            evt.setCancelled(evt.toWeatherState());
        }
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true) // Prevent thunder since rain is allowed in realms.
    public void onThunderChange(ThunderChangeEvent evt) {
        if (evt.toThunderState())
            evt.setCancelled(true);
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void preventCropTrample(PlayerInteractEvent evt) {
        //TODO replace legacy materials
        evt.setCancelled(evt.getAction() == Action.PHYSICAL && (evt.getClickedBlock().getType() == Material.LEGACY_SOIL || evt.getClickedBlock().getType() == Material.LEGACY_CROPS));
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void ignoreTallGrass(PlayerInteractEvent evt) {
        if (evt.getClickedBlock() != null) {
            evt.setCancelled(evt.getAction() == Action.LEFT_CLICK_BLOCK && evt.getClickedBlock().getType() == Material.LEGACY_LONG_GRASS);
        }
    }

    /**
     * Prevent non-player entities to use portals
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onPortal(EntityPortalEvent evt) {
        evt.setCancelled(!(evt.getEntity() instanceof Player));
    }

    /**
     * Prevent picking up arrows
     * @param evt
     */
    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent evt) {
        evt.setCancelled(true);
        evt.getArrow().remove();
    }

    /**
     * Prevent vanilla enchants in case player are somehow able to use anvils / enchanting tables
     * @param evt
     */
    @EventHandler
    public void onVanillaEnchant(EnchantItemEvent evt) {
        evt.setCancelled(true);
    }

}
