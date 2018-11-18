package com.legendsofvaleros.modules.characters.config;

/**
 * Configuration for what to do when a player logs out during combat.
 */
public interface CombatLoggingConfig {

  /**
   * Gets the number of milliseconds a player must be out of PvP combat before they are allowed to
   * log out or switch characters without penalty.
   * 
   * @return The number of milliseconds a player must wait after PvP combat before they can log out
   *         legitimately.
   */
  long getMillisUntilOutOfPvpCombat();

  /**
   * Gets the number of milliseconds a player must be out of PvE combat before they are allowed to
   * log out or switch characters without penalty.
   * 
   * @return The number of milliseconds a player must wait after PvE combat before they can log out
   *         legitimately.
   */
  long getMillisUntilOutOfPveCombat();

}
