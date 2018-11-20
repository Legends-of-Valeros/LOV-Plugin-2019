package com.legendsofvaleros.modules.combatengine.events;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event called when an attack misses.
 */
public class CombatEngineAttackMissEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final CombatEntity target;
  private final CombatEntity attacker;

  /**
   * Class constructor.
   * 
   * @param target The target of the missed attack.
   * @param attacker The attacking entity. Can be <code>null</code> if the missed attack's cause was
   *        ambiguous or not caused by another entity.
   * @throws IllegalArgumentException On a <code>null</code> target entity.
   */
  public CombatEngineAttackMissEvent(CombatEntity target, CombatEntity attacker)
      throws IllegalArgumentException {
    if (target == null) {
      throw new IllegalArgumentException("target entity cannot be null");
    }
    this.target = target;
    this.attacker = attacker;
  }

  /**
   * Gets the entity that was targeted in the missed attack.
   * 
   * @return The targeted entity.
   */
  public CombatEntity getTarget() {
    return target;
  }

  /**
   * The entity that attempted the missed attack, if any.
   * 
   * @return The attacking entity. <code>null</code> if the missed attack's cause was ambiguous or
   *         not caused by another entity.
   */
  public CombatEntity getAttacker() {
    return attacker;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
