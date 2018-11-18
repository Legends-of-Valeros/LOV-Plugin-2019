package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration about hitting/dodging chances.
 */
public interface HitChanceConfig {

  /**
   * Gets the hit chance of attacks that are not from a specific source, but are still set to be
   * able to miss.
   * 
   * @return The hit chance of attacks that come from ambiguous sources, as a decimal (
   *         <code>0.5</code> for a 50% chance).
   */
  double getUnattributedHitChance();

}
