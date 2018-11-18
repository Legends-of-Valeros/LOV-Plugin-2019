package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a status effect is successfully added to or extended for an entity.
 * <p>
 * If an effect is added that does not change the previous state of the entity in any way, this will
 * <b>not</b> be called.
 */
public class CombatEngineStatusEffectAddedEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final CombatEntity affected;
  private final StatusEffectType type;
  private long durationTicks;

  private boolean cancelled;

  /**
   * Class constructor.
   * 
   * @param affected The entity that the status effect is being added to or extended for.
   * @param effectType The type of effect being added or extended.
   * @param durationTicks The duration of the effect from now, in ticks (usually 1/20 of a second).
   * @throws IllegalArgumentException On a <code>null</code> affected entity or effect type.
   */
  public CombatEngineStatusEffectAddedEvent(CombatEntity affected, StatusEffectType effectType,
      long durationTicks) throws IllegalArgumentException {
    if (affected == null) {
      throw new IllegalArgumentException("affected entity cannot be null");
    } else if (effectType == null) {
      throw new IllegalArgumentException("effect type cannot be null");
    }

    this.affected = affected;
    this.type = effectType;
    this.cancelled = true;
    setDuration(durationTicks);
  }

  /**
   * Gets the entity that the status effect is being added to or extended for.
   * 
   * @return The affected entity.
   */
  public CombatEntity getAffectedEntity() {
    return affected;
  }

  /**
   * Gets the type of effect being added or extended.
   * 
   * @return The effect's type.
   */
  public StatusEffectType getEffectType() {
    return type;
  }

  /**
   * Gets how long from now, in ticks (usually 1/20 of a second) this effect will last for.
   * 
   * @return The effect's duration from now in ticks.
   */
  public long getDuration() {
    return durationTicks;
  }

  /**
   * Sets how long from now, in ticks (usually 1/20 of a second) this effect should last for.
   * 
   * @param durationTicks How long to set the effect's duration to, in ticks.
   */
  public void setDuration(long durationTicks) {
    if (this.durationTicks < 1 && durationTicks >= 1) {
      cancelled = false;
    } else if (this.durationTicks >= 1 && durationTicks < 1) {
      cancelled = true;
    }
    this.durationTicks = durationTicks;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
