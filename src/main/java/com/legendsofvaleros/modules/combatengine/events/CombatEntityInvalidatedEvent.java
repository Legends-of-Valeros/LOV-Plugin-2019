package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called when a combat entity object is no longer valid.
 * <p>
 * Example causes include:
 * <ul>
 * <li>A player logging out
 * <li>A mob being killed
 * <li>A mob being unloaded from memory
 * </ul>
 * <p>
 * This event may not be called as soon as an object becomes invalid. It is not safe to store combat
 * entity objects over extended periods of time.
 */
public class CombatEntityInvalidatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final UUID uid;
    private final CombatEntity entity;

    /**
     * Class constructor.
     * @param uid    The unique name of the entity whose combat object was invalidated.
     * @param entity The object that was invalidated.
     */
    public CombatEntityInvalidatedEvent(UUID uid, CombatEntity entity) {
        if (uid == null || entity == null) {
            throw new IllegalArgumentException("uid and combatentity cannot be null");
        }
        this.uid = uid;
        this.entity = entity;
    }

    /**
     * Gets the unique name of the entity whose combat object was invalidated.
     * @return The invalidated combat object's unique name.
     */
    public UUID getInvalidatedUuid() {
        return uid;
    }

    /**
     * Gets the object that was invalidated.
     * <p>
     * This object should no longer be used or stored after this point.
     * @return The invalidated object.
     */
    public CombatEntity getInvalidated() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
