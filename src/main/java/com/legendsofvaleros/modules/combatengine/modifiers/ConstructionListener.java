package com.legendsofvaleros.modules.combatengine.modifiers;

/**
 * Informed when a modifier's constructions is successfully completed.
 */
public interface ConstructionListener {

  /**
   * Called when a new modifier is successfully constructed.
   * 
   * @param newModifier The modifier that is now applied.
   */
  void onConstruction(ValueModifier newModifier);

}
