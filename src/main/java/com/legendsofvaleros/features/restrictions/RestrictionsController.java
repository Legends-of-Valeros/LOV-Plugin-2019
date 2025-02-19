package com.legendsofvaleros.features.restrictions;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.ServerMode;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.util.Discord;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;

import java.util.stream.Collectors;

/**
 * Created by Crystall on 04/11/2019
 */
public class RestrictionsController extends ListenerModule {

    /**
     * Removes endless potions in case they have some
     * @param event
     */
    @EventHandler
    public void onEndlessPotionEffect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();
        player.getActivePotionEffects().stream().map(PotionEffect::getType)
                .forEach(t -> Utilities.removeInfinitePotion(player, t));
    }

    /**
     * Semi-prevent world downloader (does not prevent world downloading overall)
     * @param evt
     */
    @EventHandler
    public void onChannelRegister(PlayerRegisterChannelEvent evt) {
        if (! evt.getChannel().equalsIgnoreCase("WDL|INIT")) {
            return;
        }
        evt.getPlayer().kickPlayer(ChatColor.RED + "Please disable World Downloader.");
        Discord.sendLogMessage(getName(),
                "**" + evt.getPlayer().getName() + "** was kicked for using World Downloader!");
    }

    /**
     * Warn staff of people that may be using boat fly
     * @param evt
     */
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent evt) {
        String fly = evt.getVehicle().getPassengers().stream().filter(Player.class::isInstance)
                .map(Entity::getName).collect(Collectors.joining(", "));
        if (evt.getVehicle().getType() == EntityType.BOAT && ! evt.getFrom().getBlock().isLiquid()
                && fly.length() > 0
                && evt.getTo().getY() > evt.getFrom().getY()
                && evt.getVehicle().getVelocity().getY() <= 0) {
            Discord.sendLogMessage(getName(),
                    "**[Anti-Cheat]** " + ChatColor.GRAY + fly + " may be using BoatFly.");
        }
    }

    /**
     * Send warnings if a staff member is using game modes on the live server
     * @param evt
     */
    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE)
                && evt.getNewGameMode() == GameMode.CREATIVE) {
            evt.setCancelled(true);
            Discord.sendLogMessage(getName(),
                    "" + evt.getPlayer().getName() + " tried to enter " + evt.getNewGameMode().name()
                            .toLowerCase() + "! This should not happen.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSwimming(EntityToggleSwimEvent event) {
        event.setCancelled(true);
        ((Player) event.getEntity()).setSwimming(false);
        //TODO set player meta data to not swimming - this fixes a bug in bukkit
//        PacketContainer meta = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
//        meta.setMeta(EntityMetadataStore.);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerGlide(EntityToggleGlideEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void preventBucketEmpty(PlayerBucketEmptyEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE)) {
            evt.setCancelled(
                    evt.getBlockClicked().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preventBucketFill(PlayerBucketFillEvent evt) {
        if (LegendsOfValeros.getMode().equals(ServerMode.LIVE)) {
            evt.setCancelled(
                    evt.getBlockClicked().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
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
        if (evt.getCause() == HangingBreakEvent.RemoveCause.PHYSICS &&
                ent.getLocation().getBlock().getRelative(evt.getEntity().getFacing().getOppositeFace())
                        .getType() == Material.AIR) {
            ent.remove();
        }
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand ||
                event.getRightClicked() instanceof Painting ||
                event.getRightClicked() instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
            if (event.getClickedBlock() == null) {
                return;
            }
            Material clickedType = event.getClickedBlock().getType();
            if (clickedType.name().endsWith("BUTTON") || clickedType.name().endsWith("TRAPDOOR")
                    || clickedType.name().endsWith("PRESSURE_PLATE")
                    || clickedType.name().endsWith("_SHULKER_BOX")
                    || clickedType.name().contains("ANVIL")
                    || clickedType.name().endsWith("FENCE_GATE")) {
                event.setCancelled(true);
                return;
            }
            switch (clickedType) {
                case WHEAT:
                case FARMLAND:
                case CHEST:
                case HOPPER:
                case FURNACE:
                case CRAFTING_TABLE:
                case ENDER_CHEST:
                case ENCHANTING_TABLE:
                case TRAPPED_CHEST:
                case DISPENSER:
                case DRAGON_EGG:
                case DROPPER:
                case BREWING_STAND:
                case POTTED_CORNFLOWER:
                case FLOWER_POT:
                case LEVER:
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
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
            evt.setCancelled(
                    evt.getBlock().getWorld().getName().equalsIgnoreCase(LegendsOfValeros.WORLD_NAME));
        }
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPortalDestroy(BlockPhysicsEvent evt) {
        evt.setCancelled((evt.getBlock().getType() == Material.NETHER_PORTAL
                && evt.getChangedType() != Material.NETHER_PORTAL)
                || (evt.getBlock().getType().equals(Material.END_PORTAL)
                && evt.getChangedType() != Material.END_PORTAL));
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
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
        if (evt.toThunderState()) {
            evt.setCancelled(true);
        }
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void preventCropTrample(PlayerInteractEvent evt) {
        evt.setCancelled(evt.getAction() == Action.PHYSICAL && (
                evt.getClickedBlock().getType() == Material.FARMLAND
                        || evt.getClickedBlock().getType() == Material.WHEAT));
    }

    /**
     * @param evt
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void ignoreTallGrass(PlayerInteractEvent evt) {
        if (evt.getClickedBlock() != null) {
            evt.setCancelled(evt.getAction() == Action.LEFT_CLICK_BLOCK
                    && evt.getClickedBlock().getType() == Material.TALL_GRASS);
        }
    }

    /**
     * Prevent non-player entities to use portals
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onPortal(EntityPortalEvent evt) {
        evt.setCancelled(! (evt.getEntity() instanceof Player));
    }

    /**
     * Prevent picking up arrows
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onArrowPickup(PlayerPickupArrowEvent evt) {
        evt.setCancelled(true);
        evt.getArrow().remove();
    }

    /**
     * Prevent vanilla enchants in case player are somehow able to use anvils / enchanting tables
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onVanillaEnchant(EnchantItemEvent evt) {
        evt.setCancelled(true);
    }

    /**
     * prevent crafting of vanilla items
     * @param evt
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemCraft(CraftItemEvent evt) {
        evt.setCancelled(true);
    }

}
