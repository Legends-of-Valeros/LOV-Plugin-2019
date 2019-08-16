package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when combat information has just been created for an entity.
 * <p>
 * Called before any clients have had a chance to access and use these values. Allows for stat and
 * other info to be initialized before the data is used.
 */
public class CombatEntityCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final LivingEntity entity;
    private final CombatEntity combatEntity;

    /**
     * Class constructor.
     * @param combatEntity The created combat data object.
     * @throws IllegalArgumentException On a <code>null</code> parameter.
     */
    public CombatEntityCreateEvent(CombatEntity combatEntity) throws IllegalArgumentException {
        if (combatEntity == null) {
            throw new IllegalArgumentException("combat entity cannot be null");
        }
        this.entity = combatEntity.getLivingEntity();
        this.combatEntity = combatEntity;
    }

    /**
     * Gets the entity that the combat data was created for.
     * @return The entity this event is for.
     */
    public LivingEntity getLivingEntity() {
        return entity;
    }

    /**
     * Gets the combat data object that was created for the entity.
     * @return The entity's combat data.
     */
    public CombatEntity getCombatEntity() {
        return combatEntity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
