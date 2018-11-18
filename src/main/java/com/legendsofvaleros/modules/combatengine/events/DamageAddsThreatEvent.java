package com.legendsofvaleros.modules.combatengine.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

/**
 * An event called when an mob's threat level for another entity changes as the direct, automated
 * result of damage being dealt.
 * <p>
 * This is <b>NOT</b> called every time threat changes, but only when threat is added as the direct
 * result of damage being dealt. It is not called in situations like when threat automatically
 * decreases over time, or if threat is added manually through the threat API.
 * <p>
 * Threat is a measurement of how high priority a given enemy should be. The enemy entity with the
 * highest threat will be an attacking entity's highest-priority target (unless they are a human and
 * have free will).
 */
public class DamageAddsThreatEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final CombatEntity targeter;
  private final CombatEntity possibleTarget;
  private final double previousThreatLevel;
  private double threatAdded;

  private boolean cancelled;

  /**
   * Class constructor.
   * 
   * @param targeter The entity that was attacked and whose threat level for the attacker is being
   *        added to.
   * @param possibleTarget The attacking entity whose threat level is being added to.
   * @param previousThreatLevel The previous threat level of the attacked entity for the attacker.
   * @param threatChangingBy The amount of threat being added as the result of the attack.
   */
  public DamageAddsThreatEvent(CombatEntity targeter, CombatEntity possibleTarget,
      double previousThreatLevel, double threatChangingBy) {
    if (targeter == null || possibleTarget == null) {
      throw new IllegalArgumentException("targeter and possible target cannot be null");
    }
    this.targeter = targeter;
    this.possibleTarget = possibleTarget;
    this.previousThreatLevel = previousThreatLevel;
    this.threatAdded = threatChangingBy;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  /**
   * Gets the entity who was attacked and whose threat perception is being changed towards another
   * entity as a result.
   * <p>
   * The targeter is the entity whose target might change as a result of the attack that happened
   * and this resulting threat level increase.
   * 
   * @return The targeting entity.
   */
  public CombatEntity getTargeter() {
    return targeter;
  }

  /**
   * Gets the attacking entity who may be targeted as a result of this threat level increase.
   * 
   * @return The entity who attacked the targeting entity and whose threat level towards it is being
   *         changed.
   */
  public CombatEntity getPossibleTarget() {
    return possibleTarget;
  }

  /**
   * Gets what the threat level is before the threat in this event is added.
   * 
   * @return The previous threat level.
   */
  public double getPreviousThreatLevel() {
    return previousThreatLevel;
  }

  /**
   * Gets the amount of threat being added.
   * 
   * @return The amount of threat being added.
   */
  public double getThreatAdded() {
    return threatAdded;
  }

  /**
   * Sets the amount of threat being added.
   * 
   * @param threat The new amount of threat to add.
   */
  public void setThreatAdded(double threat) {
    this.threatAdded = threat;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

}
