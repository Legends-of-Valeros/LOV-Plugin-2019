package com.legendsofvaleros.modules.characters.entityclass;

import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

/**
 * A model from which to create per-player stat modifiers whose value is governed by a separate
 * variable value.
 */
public final class StatModifierModel {

  private final Stat modify;
  private final ValueModifierBuilder.ModifierType modType;
  private final double value;

  public StatModifierModel(Stat modify, ValueModifierBuilder.ModifierType modType, double valuePerPoint) {
    if (modify == null || modType == null) {
      throw new IllegalArgumentException("parameters cannot be null");
    }
    this.modify = modify;
    this.modType = modType;
    this.value = valuePerPoint;
  }

  /**
   * Gets the stat that derived modifiers should modify.
   * 
   * @return The stat to modify.
   */
  public Stat getStat() {
    return modify;
  }

  /**
   * Gets the type of modifier that derived modifiers should be.
   * 
   * @return The type of modifier.
   */
  public ValueModifierBuilder.ModifierType getModifierType() {
    return modType;
  }

  /**
   * Gets the value that should be multiplied by the separate governing stat to define the ultimate
   * value of the derived multiplier.
   * <p>
   * The formula should be <code>valuePerPoint * pointsOfGoverningOutsideStat = modifierValue</code>
   * 
   * @return The value of the modifier per point of an outside, governing stat.
   */
  public double getValue() {
    return value;
  }

}
