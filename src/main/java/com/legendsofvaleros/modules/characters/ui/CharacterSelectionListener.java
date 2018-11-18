package com.legendsofvaleros.modules.characters.ui;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import org.bukkit.entity.Player;

import com.legendsofvaleros.modules.characters.api.CharacterId;

/**
 * Informed of players selecting characters to play.
 */
public interface CharacterSelectionListener {

  /**
   * Called when a player selects a character they want to play, either after logging in or in order
   * to switch from the character they are already playing to another one.
   * 
   * @param player The player that made the selection.
   * @param characterId The name of the player that was selected.
   * @return <code>true</code> if the selection was legitimate and the interface should be allowed
   *         to close/change. <code>false</code> if the selection was invalid or impossible and the
   *         interface should be maintained in order for the player to make a valid selection.
   */
  boolean onCharacterSelected(Player player, CharacterId characterId);

  /**
   * Called when a player removes a character
   * 
   * @param player The player that made the selection.
   * @param characterId The name of the player that was removed.
   * @return <code>true</code> if the selection was legitimate and the interface should be allowed
   *         to close/change. <code>false</code> if the selection was invalid or impossible and the
   *         interface should be maintained in order for the player to make a valid selection.
   */
  boolean onCharacterRemoved(Player player, CharacterId characterId);

  /**
   * Called when a player attempts to create a new character.
   * 
   * @param player The player who is attempting to create a new character.
   * @return <code>true</code> if the selection was legitimate and the interface should be allowed
   *         to close/change. <code>false</code> if the selection was invalid or impossible and the
   *         interface should be maintained in order for the player to make a valid selection.
   */
  boolean onNewCharacterSelected(Player player, int number);

}
