package com.legendsofvaleros.modules.combatengine.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Called when CombatEngine cancels a vanilla damage event because it was not caused by
 * CombatEngine.
 * <p>
 * Called during the <code>EventPriority.MONITOR</code> phase of the cancelled event.
 */
public class VanillaDamageCancelledEvent extends Event implements Cancellable {
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();

    private final EntityDamageEvent event;
    private final boolean byEntity;

    /**
     * Class constructor.
     * @param event The cancelled damage event.
     * @throws IllegalArgumentException On a <code>null</code> event.
     */
    public VanillaDamageCancelledEvent(EntityDamageEvent event) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }
        this.event = event;
        this.byEntity = event instanceof EntityDamageByEntityEvent;
    }

    /**
     * Gets the damage event cancelled because it was not caused by CombatEngine.
     * <p>
     * Can be used to translate vanilla damage instances to CombatEngine damage through the
     * CombatEngine API.
     * @return The cancelled event.
     */
    public EntityDamageEvent getCancelledEvent() {
        return event;
    }

    /**
     * Gets whether the cancelled damage event was directly caused by an entity.
     * @return <code>true</code> if the cancelled damage event was an instance of
     * <code>EntityDamageByEntityEvent</code>, else <code>false</code>.
     */
    public boolean isDamageByEntity() {
        return byEntity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
