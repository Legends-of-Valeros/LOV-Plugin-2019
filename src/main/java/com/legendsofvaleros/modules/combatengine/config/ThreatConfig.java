package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration for threat and targeting AI.
 */
public interface ThreatConfig {

  /**
   * Gets the square of maximum distance between two entities where one can still be threatened by
   * and target the other.
   * 
   * @return The max targeting distance.
   */
  double getMaxTargetingDistanceSquared();

  /**
   * Gets the interval between target validation checks, in ticks.
   * 
   * @return The time between each target validation check.
   */
  long getValidationCheckTicks();

  /**
   * Gets how much threat should be reduced by per check interval, as a percentage of how much
   * threat was present the last time threat was added/increased.
   * 
   * @return A percentage of the last time threat was increased to reduce threat levels by each
   *         check, as a decimal (ex: <code>0.5</code> for 50%).
   */
  double getThreatReductionPerCheck();

}
