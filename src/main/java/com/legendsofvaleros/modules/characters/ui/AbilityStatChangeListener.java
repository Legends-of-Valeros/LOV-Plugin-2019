package com.legendsofvaleros.modules.characters.ui;

import com.legendsofvaleros.modules.classes.stats.AbilityStat;

/**
 * Informed of changes in class stats.
 */
public interface AbilityStatChangeListener {

  /**
   * Called when a class stat's value changes.
   * 
   * @param changed The class stat that changed.
   * @param newValue The class stat's new value.
   * @param previousValue The class stat's previous value.
   */
  void onAbilityStatChange(AbilityStat changed, double newValue, double previousValue);

}
