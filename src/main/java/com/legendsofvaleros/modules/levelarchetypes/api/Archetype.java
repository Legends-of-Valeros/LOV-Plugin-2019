package com.legendsofvaleros.modules.levelarchetypes.api;

import com.legendsofvaleros.modules.combatengine.core.CombatProfile;

/**
 * A broad category of mobs/players on the server.
 * <p>
 * Contains general information like stats that can be used as a simplified, centralized baseline
 * for creating large quantities of content. By centralizing baselines into archetypes, it is much
 * easier to maintain balance across vastly varying and large quantities of content.
 */
public interface Archetype {

  /**
   * Gets the id of this archetype, as it appears in the configuration.
   * 
   * @return The archetype's configured id.
   */
  String getId();

  /**
   * Gets the base CombatEngine stats of this archetype at a given level.
   * 
   * @param level The level to get base stats for.
   * @return The base CombatEngine stats for an entity of this archetype at the given level.
   * @see CombatProfile
   */
  CombatProfile getCombatProfile(int level);

  /**
   * Gets the level of a stat of this archetype at a given level.
   * <p>
   * LevelArchetypes supports the configuration of arbitrary sets of stats in addition to things
   * like CombatEngine stats.
   * 
   * @param statName The name of the stat, as it appears in the configuration.
   * @param level The level of this archetype at which to get the value of the desired stat.
   * @return The value of the given stat at the given level. <code>0</code> if the stat is not
   *         configured for this archetype.
   */
  double getStatValue(String statName, int level);

}
