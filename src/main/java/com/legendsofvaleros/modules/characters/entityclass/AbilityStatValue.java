package com.legendsofvaleros.modules.characters.entityclass;

import com.legendsofvaleros.modules.combatengine.modifiers.ModifiableDouble;
import com.legendsofvaleros.modules.characters.ui.AbilityStatChangeListener;
import com.legendsofvaleros.modules.combatengine.modifiers.ModifiableDouble;
import com.legendsofvaleros.modules.combatengine.modifiers.ModifiableDouble;

/**
 * Value of a class stat as the result of multiple factors.
 * 
 * @see AbilityStat
 */
public class AbilityStatValue extends ModifiableDouble {

  private final AbilityStat type;
  private final AbilityStatChangeListener listener;

  public AbilityStatValue(AbilityStat abilityStat, AbilityStatChangeListener listener) {
    super();
    if (abilityStat == null) {
      throw new IllegalArgumentException("class stat cannot be null");
    }
    this.type = abilityStat;
    this.listener = listener;
  }

  @Override
  protected double sanitizeValue(double sanitize) {
    return type.sanitizeValue(sanitize);
  }

  @Override
  protected void onChange(double newValue, double previousValue) {
    if (listener != null) {
      listener.onAbilityStatChange(type, newValue, previousValue);
    }
  }

}
