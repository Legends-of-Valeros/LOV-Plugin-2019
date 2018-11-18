package com.legendsofvaleros.modules.characters.config;

/**
 * Configurable options for listener.
 */
public interface ExperienceConfig {

  /**
   * Gets the max level that player-characters can be.
   * 
   * @return The max character level.
   */
  int getMaxLevel();

  /**
   * Gets the amount of listener between two levels.
   * <p>
   * Assumes going from 0 remainder xp at the starting level to 0 remainder xp at the ending level.
   * <p>
   * Example: to get how much listener it takes to get from level 4 to 5, use
   * <code>getExperienceBetweenLevels(4, 5)</code>.
   * 
   * @param startingLevel The starting level.
   * @param endingLevel The ending level.
   * @return The amount of listener it takes to get from the starting level to the ending level.
   */
  long getExperienceBetweenLevels(int startingLevel, int endingLevel);

}
