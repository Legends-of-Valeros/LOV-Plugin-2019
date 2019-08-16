package com.legendsofvaleros.modules.combatengine.api;

import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;

/**
 * Stat levels for an entity.
 * <p>
 * For dealing damage to entities, see the main CombatEngine interface.
 * 
 * @see CombatEngineAPI
 */
public interface  EntityStats {

  /**
   * Gets the combat data these stats are a part of.
   * 
   * @return The combat data that contains these stats.
   */
  CombatEntity getCombatEntity();

  /**
   * Gets the current value of a regenerating stat.
   * 
   * @param stat The stat to get the value of for the entity.
   * @return The entity's current value of the stat.
   */
  double getRegeneratingStat(RegeneratingStat stat);

  /**
   * Gets the current, final value of a stat, taking all modifiers and multipliers into account.
   * 
   * @param stat The stat to get the value of for the entity.
   * @return The entity's final value of the stat.
   */
  double getStat(Stat stat);

  /**
   * Edits the value of a regenerating stat.
   * <p>
   * Cannot edit a regenerating stat to be less than <code>0</code> or more than its max value.
   * <p>
   * To deal damage to an entity, see the main CombatEngine interface.
   * 
   * @param stat The stat whose value to modify.
   * @param modifier The amount to modify the stat by. A positive number to increase it; a negative
   *        number to reduce it
   * @see RegeneratingStat
   * @see CombatEngineAPI
   */
  void editRegeneratingStat(RegeneratingStat stat, double modifier);

  /**
   * Sets the value of a regenerating stat to a specific value.
   * <p>
   * Cannot set a regenerating stat to be less than <code>0</code> or more than its max value.
   * <p>
   * To deal damage to an entity, see the main CombatEngine interface.
   * 
   * @param stat The stat whose value to set.
   * @param setTo The new value of the stat. Will be sanitized so it is not less than <code>0</code>
   *        and not more than the stat's maximum value.
   * @see RegeneratingStat
   * @see CombatEngineAPI
   */
  void setRegeneratingStat(RegeneratingStat stat, double setTo);

  /**
   * Gets a new stat-modifier builder object with which to construct a modifier for one of these
   * stats.
   * 
   * @return An object to construct a modifier to one of the entity's stats.
   * @throws IllegalArgumentException On a <code>null</code> stat.
   */
  ValueModifierBuilder newStatModifierBuilder(Stat modify) throws IllegalArgumentException;

}
