package com.legendsofvaleros.modules.combatengine.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.combatengine.core.CombatProfile;

/**
 * Called when combat information is about to be created for an entity.
 */
public class CombatEntityPreCreateEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final LivingEntity entity;
  private CombatProfile profile;

  /**
   * Class constructor.
   * 
   * @param creatingFor The entity that a combat data object is being created for.
   * @param defaultProfile The profile that the entity's stats will be copied from initially, if
   *        any.
   * @throws IllegalArgumentException On a <code>null</code> entity.
   */
  public CombatEntityPreCreateEvent(LivingEntity creatingFor, CombatProfile defaultProfile)
      throws IllegalArgumentException {
    if (creatingFor == null) {
      throw new IllegalArgumentException(
          "entity that combat data is being created for cannot be null");
    }
    this.entity = creatingFor;
    this.profile = defaultProfile;
  }

  /**
   * Gets the entity that a combat data object is being created for.
   * 
   * @return The entity this event is for.
   */
  public LivingEntity getLivingEntity() {
    return entity;
  }

  /**
   * Sets the profile that the entity's combat data should be copied from.
   * 
   * @param profile The defaults for the entity's stats.
   */
  public void setCombatProfile(CombatProfile profile) {
    this.profile = profile;
  }

  /**
   * Gets the profile that the entity's combat data will be copied from.
   * 
   * @return The defaults for the entity's stats. <code>null</code> if no defaults are set.
   */
  public CombatProfile getCombatProfile() {
    return profile;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
