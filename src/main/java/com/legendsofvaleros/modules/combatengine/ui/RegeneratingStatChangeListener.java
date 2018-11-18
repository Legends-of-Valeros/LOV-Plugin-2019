package com.legendsofvaleros.modules.combatengine.ui;

import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;

/**
 * Informed of changes in a regenerating stat.
 */
public interface RegeneratingStatChangeListener {

  /**
   * Called when a regenerating stat's value changes.
   * 
   * @param changed The regenerating stat whose value changed.
   * @param newValue The stat's new value.
   * @param oldValue The stat's previous value.
   */
  void onRegeneratingStatChange(RegeneratingStat changed, double newValue, double oldValue);

}
