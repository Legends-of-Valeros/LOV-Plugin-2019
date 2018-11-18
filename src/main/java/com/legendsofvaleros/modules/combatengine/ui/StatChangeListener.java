package com.legendsofvaleros.modules.combatengine.ui;

import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

/**
 * Informed of changes in stats.
 */
public interface StatChangeListener {

  /**
   * Called when a stat is changed.
   * 
   * @param changed The stat that was changed.
   * @param newValue The stat's new value.
   * @param oldValue The stat's previous value.
   */
  void onStatChange(Stat changed, double newValue, double oldValue);

}
