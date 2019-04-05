package com.legendsofvaleros.modules.combatengine.statuseffects;

import com.legendsofvaleros.modules.combatengine.CombatEngine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Stops certain entities from interacting in a number of ways.
 */
public class NoInteractionListener implements Listener {

    private Set<UUID> affected;

    NoInteractionListener(Set<UUID> affected) {
        this.affected = affected;
        CombatEngine.getInstance().registerEvents(this);
    }

    private void handlePlayerEvent(PlayerEvent event) {
        if (event instanceof Cancellable && affected.contains(event.getPlayer().getUniqueId())) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    private void handleEntityEvent(EntityEvent event) {
        if (event instanceof Cancellable && affected.contains(event.getEntity().getUniqueId())) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityTarget(EntityTargetEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityInteract(EntityInteractEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityMount(EntityMountEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityShootBow(EntityShootBowEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHorseJump(HorseJumpEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        handleEntityEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (affected.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
