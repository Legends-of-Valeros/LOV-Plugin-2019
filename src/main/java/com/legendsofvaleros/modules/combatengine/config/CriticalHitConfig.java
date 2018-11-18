package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration for critical hits.
 */
public interface CriticalHitConfig {

  /**
   * Gets how much damage a critical hit does, as a multiplier of the damage's base value.
   * 
   * @return The damage multiplier for critical hits.
   */
  double getCritMultiplier();

  /**
   * Gets the crit chance of attacks that are not from a specific source.
   * 
   * @return The crit chance of attacks that come from ambiguous sources, as a decimal (
   *         <code>0.5</code> for a 50% chance).
   */
  double getUnattributedCritChance();

}
