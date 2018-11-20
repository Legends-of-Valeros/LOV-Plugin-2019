package com.legendsofvaleros.modules.characters.api;

import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;

/**
 * A collection of the player's stats that have player-class-specific effects on combat and other
 * features.
 */
public interface AbilityStats {

  /**
   * Gets the PlayerCharacter that this class stat data is a part of.
   * 
   * @return The player character that owns/contains these class stats.
   */
  PlayerCharacter getPlayerCharacter();

  /**
   * Gets the current, final value of a class stat, taking all modifiers and multipliers into
   * account.
   * <p>
   * Class stats are stats that have a different effect on combat and other features, depending on
   * what gameplay class the player-character is.
   * 
   * @param abilityStat The player-class-specific stat to get the value of for the player character.
   * @return The entity's final value of the stat.
   */
  double getAbilityStat(AbilityStat abilityStat);

  /**
   * Gets a new class-stat-modifier builder object with which to construct a modifier for one of
   * these stats that have player-class specific effects.
   * 
   * @return An object to construct a modifier to one of the player-character's stats that have
   *         class-specific effects.
   * @throws IllegalArgumentException On a <code>null</code> class stat.
   */
  ValueModifierBuilder newAbilityStatModifierBuilder(AbilityStat modify)
      throws IllegalArgumentException;

}
