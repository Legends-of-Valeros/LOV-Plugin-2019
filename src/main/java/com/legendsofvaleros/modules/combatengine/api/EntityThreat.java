package com.legendsofvaleros.modules.combatengine.api;

import org.bukkit.entity.LivingEntity;

/**
 * Tracks threat levels against this entity.
 * <p>
 * Threat is a measurement of how high priority a given enemy should be. The entity with the highest
 * threat should be this entity's highest-priority target (unless they are a human and have free
 * will).
 */
public interface EntityThreat {

  /**
   * The current, highest-priority target of this entity. The entity that has generated the most
   * threat.
   * 
   * @return The enemy entity that this entity should be targeting. <code>null</code> if the entity
   *         does not have a target.
   */
  CombatEntity getTarget();

  /**
   * Gets how much threat an entity has generated towards this entity. The higher the threat, the
   * more likely it is that this entity will target the given entity.
   * <p>
   * By default, <code>1.0</code> damage done = <code>1.0</code> threat. Threat decays over time, so
   * while doing <code>500.0</code> might generate <code>500.0</code> threat in the short term, over
   * time that amount will be reduced and eventually reach a minimal level if more threat is not
   * generated in the meantime.
   * 
   * @param threatFrom The enemy entity for which to get the threat level towards this entity.
   * @return The threat level of this entity towards the given enemy entity.
   */
  double getThreat(LivingEntity threatFrom);

  /**
   * Adds to/subtracts from the amount of threat this entity feels towards a given enemy entity.
   * 
   * @param threatFrom The enemy entity whose threat level to edit for this entity.
   * @param amount The amount to edit the threat level by. Positive to add threat (a higher chance
   *        of being targeted), negative to remove threat (a lower chance of being targeted).
   */
  void editThreat(LivingEntity threatFrom, double amount);

  /**
   * Sets the level of threat that this entity feels towards a given enemy entity.
   * 
   * @param threatFrom The entity whose threat level to set for this entity.
   * @param amount The new threat level. Threat cannot go below <code>0</code>. The higher the
   *        threat level, the more chance that this entity will target the given enemy entity.
   */
  void setThreat(LivingEntity threatFrom, double amount);

}
