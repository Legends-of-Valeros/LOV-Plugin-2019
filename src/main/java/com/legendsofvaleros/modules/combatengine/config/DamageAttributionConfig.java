package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration for damage and kill attribution.
 */
public interface DamageAttributionConfig {

  /**
   * Gets the maximum amount of time that damage history will be stored for.
   * 
   * @return The max lifespan of an instance of damage, in milliseconds, after which it will not be
   *         considered for kill attribution.
   */
  long getHistoryExpirationMillis();

  /**
   * Gets the maximum distance a player can be from the entity they killed in order to get credit
   * for the kill.
   * 
   * @return The max kill distance.
   */
  double getMaxKillDistance();

}
