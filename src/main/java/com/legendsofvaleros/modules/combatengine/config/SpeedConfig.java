package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration for the speed stat and its effects.
 */
public interface SpeedConfig {

  /**
   * Gets the level of the speed stat at which Minecraft entities move their normal speeds.
   * 
   * @return The normal speed stat where entities' speed will not be modified.
   */
  double getNormalSpeed();

  /**
   * Gets the number of speed points either above or below normal speed an entity's speed stat must
   * be in order to be given a speed or slow potion effect.
   * <p>
   * How many multiples of this number above or below the normal speed stat an entity's speed stat
   * is will define the level of their speed/slowness potion effect.
   * <p>
   * Potion effects are always rounded down. If the normal speed stat <code>100.0</code>, the value
   * returned by this method is <code>10.0</code>, and an entity has a <code>90.001</code> speed
   * stat, they will not yet have a slowness speed effect. In this example, they must have a speed
   * stat of <code>90.0</code> or less before the slowness effect is applied. The same would apply
   * for speed: if the example entity had a <code>109.999</code> speed stat, they would not get a
   * speed potion effect until they had <code>110.0</code> or more.
   * 
   * @return The number of speed stat points per potion level increment.
   */
  double getSpeedPointsPerPotionLevel();

}
