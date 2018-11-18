package com.legendsofvaleros.modules.combatengine.config;

/**
 * Configuration of values for offensive and defensive stats.
 */
public interface OffenseDefenseConfig {

  /**
   * Gets the percentage that physical-damage is increased per point of the physical-damage stat.
   * 
   * @return A decimal (ex: 0.1 = 10%) of the percentage that spell-damage is increased per point of
   *         the physical-damage stat.
   */
  double getPhysicalDamageIncrease();

  /**
   * Gets the percentage that spell-damage is increased per point of the magic-damage stat.
   * 
   * @return A decimal (ex: 0.1 = 10%) of the percentage that spell-damage is increased per point of
   *         the magic-damage stat.
   */
  double getMagicDamageIncrease();

  /**
   * Gets the percentage of spell-damage reduction per point of armor.
   * 
   * @return A decimal (ex: 0.1 = 10%) of the percentage in spell-damage reduction per point of
   *         armor.
   */
  double getArmorSpellDamageReduction();

  /**
   * Gets the percentage of physical-damage reduction per point of armor.
   * 
   * @return A decimal (ex: 0.1 = 10%) of the percentage in physical-damage reduction per point of
   *         armor.
   */
  double getArmorPhysicalDamageReduction();

  /**
   * Gets the percentage of spell-damage reduction per point of a spell's resistance stat.
   * 
   * @return A decimal (ex: 0.1 = 10%) of the percentage in spell-damage reduction per point of a
   *         spell's resistance stat
   */
  double getResistanceSpellDamageReduction();

}
