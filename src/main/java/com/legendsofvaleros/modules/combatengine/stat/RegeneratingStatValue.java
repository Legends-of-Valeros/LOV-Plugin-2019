package com.legendsofvaleros.modules.combatengine.stat;

import com.legendsofvaleros.modules.combatengine.ui.RegeneratingStatChangeListener;

/**
 * A value of a stat as that regenerates over time and uses another stat's value as its upper limit.
 */
public class RegeneratingStatValue {

  private final RegeneratingStat type;
  private final StatValue max;
  private final RegeneratingStatChangeListener listener;

  private double value;

  public RegeneratingStatValue(RegeneratingStat stat, StatValue maxStat, double startingValue,
      RegeneratingStatChangeListener listener) {
    this.type = stat;
    this.max = maxStat;
    this.listener = listener;

    editValue(startingValue);
  }

  /**
   * Gets the current value of this stat.
   * 
   * @return This stat's current value.
   */
  public double getCurrentValue() {
    return value;
  }

  /**
   * Edits this value.
   * 
   * @param modifier The amount to add to this value. Negative to reduce its value.
   */
  public void editValue(double modifier) {
    double previousValue = value;

    value += modifier;
    checkValue();

    updateListener(previousValue);
  }

  /**
   * Sets this value.
   * 
   * @param setTo The target value. Will be sanitized.
   */
  public void setValue(double setTo) {
    double previousValue = value;

    value = setTo;
    checkValue();

    updateListener(previousValue);
  }

  /**
   * Checks the current value against its possible maximum and minimum, making any necessary changes
   * to stay valid.
   */
  public void checkValue() {
    if (value > max.getFinalValue()) {
      value = max.getFinalValue();
    } else if (value < 0.0) {
      value = 0.0;
    }
  }

  private void updateListener(double previousValue) {
    if (listener != null && previousValue != value) {
      listener.onRegeneratingStatChange(type, value, previousValue);
    }
  }

}
