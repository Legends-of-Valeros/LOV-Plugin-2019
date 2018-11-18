package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration for player respawns.
 */
public interface RespawnConfig {

  /**
   * Gets the percentage of their max health that a player should respawn with.
   * 
   * @return A decimal of the percent of max health (ex: 0.5 = 50%) players should respawn with.
   */
  double getRespawnHealthPercentage();

  /**
   * Gets the percentage of their max mana that a player should respawn with.
   * 
   * @return A decimal of the percent of max mana (ex: 0.5 = 50%) players should respawn with.
   */
  double getRespawnManaPercentage();

  /**
   * Gets the percentage of their max energy that a player should respawn with.
   * 
   * @return A decimal of the percent of max energy (ex: 0.5 = 50%) players should respawn with.
   */
  double getRespawnEnergyPercentage();

}
