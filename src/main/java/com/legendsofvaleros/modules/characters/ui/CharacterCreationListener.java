package com.legendsofvaleros.modules.characters.ui;

import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.classes.EntityClass;
import org.bukkit.entity.Player;

/**
 * Informed when a player successfully creates a new character.
 */
public interface CharacterCreationListener {

  /**
   * Called when a player has selected the minimum required options and then finalized/confirmed
   * their choice.
   * <p>
   * Marks the end of character creation, and that the player should start playing the character
   * they created.
   *
   * @param player The player who completed character selection.
   * @param raceSelected The race that the player selected.
   * @param classSelected The class that the player selected.
   */
  void onOptionsFinalized(Player player, int number, EntityRace raceSelected, EntityClass classSelected);

}
