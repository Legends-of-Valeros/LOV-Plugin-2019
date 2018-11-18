package com.legendsofvaleros.modules.combatengine.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

/**
 * Called when the highest-priority target of a mob changes.
 * <p>
 * Priority is based on threat, which is generated by dealing damage to a mob, but also can be
 * edited and generated directly in order to manipulate the priority of a mob's targets.
 */
public class AIPriorityTargetChangeEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final CombatEntity ai;
  private final CombatEntity newTarget;
  private final CombatEntity previousTarget;

  /**
   * Class constructor.
   * 
   * @param targeter The mob whose target changed.
   * @param newTarget The new target. Can be <code>null</code> if the old target was invalidated but
   *        there is no new target.
   * @param previousTarget The previous target. Can be <code>null</code> if there is no previous
   *        target.
   * @throws IllegalArgumentException On a <code>null</code> targeter, a targeter that is a player,
   *         or if both the previous and new targets are <code>null</code>.
   */
  public AIPriorityTargetChangeEvent(CombatEntity targeter, CombatEntity newTarget,
      CombatEntity previousTarget) throws IllegalArgumentException {

    if (targeter == null || targeter.getLivingEntity() == null || targeter.isPlayer()) {
      throw new IllegalArgumentException("targeter cannot be null or a player");
    } else if (newTarget == null && previousTarget == null) {
      throw new IllegalArgumentException("either the new or previous target must be not null");
    }

    this.ai = targeter;
    this.newTarget = newTarget;
    this.previousTarget = previousTarget;
  }

  /**
   * Gets the entity whose target is changing.
   * 
   * @return The targeting non-player entity.
   */
  public CombatEntity getTargeter() {
    return ai;
  }

  /**
   * Gets the new target of the targeting entity.
   * 
   * @return The new target. <code>null</code> if the previous target was invalidated, but there is
   *         no new target available.
   */
  public CombatEntity getNewTarget() {
    return newTarget;
  }

  /**
   * Gets the previous target of the targeting entity.
   * 
   * @return The previous target. <code>null</code> if the targeting entity was not targeting
   *         anything before they got a new target.
   */
  public CombatEntity getPreviousTarget() {
    return previousTarget;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
