package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType.RemovalReason;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a status effect is removed or expires.
 */
public class CombatEngineStatusEffectRemovedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final CombatEntity affected;
  private final StatusEffectType type;
  private final RemovalReason reason;

  /**
   * Class constructor.
   * 
   * @param affected The entity that the status effect is being removed from or is expiring for.
   * @param effectType The type of effect being removed or expiring.
   * @throws IllegalArgumentException On a <code>null</code> affected entity, effect type, or
   *         reason.
   */
  public CombatEngineStatusEffectRemovedEvent(CombatEntity affected, StatusEffectType effectType,
      RemovalReason reason) throws IllegalArgumentException {
    if (affected == null) {
      throw new IllegalArgumentException("affected entity cannot be null");
    } else if (effectType == null) {
      throw new IllegalArgumentException("effect type cannot be null");
    } else if (reason == null) {
      throw new IllegalArgumentException("removal reason cannot be null");
    }

    this.affected = affected;
    this.type = effectType;
    this.reason = reason;
  }

  /**
   * Gets the entity that the status effect is being removed from or is expiring for.
   * 
   * @return The affected entity.
   */
  public CombatEntity getAffectedEntity() {
    return affected;
  }

  /**
   * Gets the type of effect that is being removed or expired.
   * 
   * @return The effect's type.
   */
  public StatusEffectType getEffectType() {
    return type;
  }

  /**
   * Gets the reason the effect is being removed or expiring.
   * 
   * @return The reason for the status effect being removed.
   */
  public RemovalReason getRemovalReason() {
    return reason;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
