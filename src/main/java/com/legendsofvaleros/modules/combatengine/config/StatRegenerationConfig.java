package com.legendsofvaleros.modules.combatengine.config;

import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;

/**
 * Configuration for stat regen settings.
 */
public interface StatRegenerationConfig {

  /**
   * Gets how many ticks should pass in between each regeneration event for all entities.
   * 
   * @return The number of ticks in between each natural regen.
   */
  long getRegenIntervalTicks();

  /**
   * Gets how much regen stats should restore the stats they are regenerating.
   * 
   * @param stat The stat that is being regenerated.
   * @return How much the stat's regen version's value should restore the stat each interval as a
   *         percentage-of-max-per-point. For example, if a player's health regen is
   *         <code>125.0</code> and this returns <code>0.01</code>, then the player will regen
   *         <code>(125.0 * 0.01) == 1.25</code>% of their max health every regen interval.
   */
  double getRegenPercentagePerPoint(RegeneratingStat stat);

}
