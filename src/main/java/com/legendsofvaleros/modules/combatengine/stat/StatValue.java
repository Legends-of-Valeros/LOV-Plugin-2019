package com.legendsofvaleros.modules.combatengine.stat;

import com.legendsofvaleros.modules.combatengine.modifiers.ModifiableDouble;
import com.legendsofvaleros.modules.combatengine.ui.StatChangeListener;

/**
 * A value of a stat as the result of multiple factors.
 * 
 * @See Stat
 */
public class StatValue extends ModifiableDouble {

  private final Stat type;
  private final StatChangeListener listener;

  /**
   * Class constructor.
   * 
   * @param stat The type of stat this value is for.
   * @param baseValue The starting value of this stat. <b>Will</b> be affected by multipliers.
   * @param listener A listener to inform of changes to this stat value.
   */
  public StatValue(Stat stat, double baseValue, StatChangeListener listener) {
    super();
    if (stat == null) {
      throw new IllegalArgumentException("stat cannot be null");
    }
    this.type = stat;
    flatEdit(baseValue, false);
    this.listener = listener;
  }

  @Override
  protected double sanitizeValue(double sanitize) {
    return type.sanitizeValue(sanitize);
  }

  @Override
  protected void onChange(double newValue, double previousValue) {
    if (listener != null) {
      listener.onStatChange(type, newValue, previousValue);
      
    }
  }

}
