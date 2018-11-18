package com.legendsofvaleros.modules.combatengine.modifiers;

import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;


/**
 * A modifier with a simple value that can be edited after it is initially created.
 */
public class EditableValueModifier extends ValueModifier {

  EditableValueModifier(ModifiableDouble modify, ModifierType type, double startingValue,
      boolean removeOnDeath) {
    super(modify, type, startingValue, removeOnDeath);
  }

  EditableValueModifier(ModifiableDouble modify, ModifierType type, double startingValue,
      boolean removeOnDeath, long durationTicks) {
    super(modify, type, startingValue, removeOnDeath, durationTicks);
  }

  /**
   * Sets the current value of this modifier.
   * 
   * @param value The value to set the modifier to.
   */
  public void setValue(double value) {
    switch (getType()) {
      case FLAT_EDIT:
        valueModifying.flatEdit(value - currentModifier, false);
        break;

      case FLAT_EDIT_IGNORES_MULTIPLIERS:
        valueModifying.flatEdit(value - currentModifier, true);
        break;

      case MULTIPLIER:
        valueModifying.editMultiplier(currentModifier, value);
        break;
    }

    currentModifier = value;
  }

  @Override
  protected void onRemove() {}

}
