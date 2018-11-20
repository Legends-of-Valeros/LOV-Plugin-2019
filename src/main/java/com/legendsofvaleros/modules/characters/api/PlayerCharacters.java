package com.legendsofvaleros.modules.characters.api;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Multi-character data for a player.
 */
public interface PlayerCharacters {

	/**
	 * The name of the player these characters are for.
	 * 
	 * @return The player's unique name.
	 */
	UUID getPlayerId();

	/**
	 * Gets the player that these characters are for, if they are still in memory.
	 * 
	 * @return The player these characters are for, if they are in memory. Else <code>null</code>.
	 */
	Player getPlayer();
	
	int getMaxCharacters();

	/**
	 * Gets the current character that the player is using.
	 * 
	 * @return The player's current character. <code>null</code> if the player is not currently
	 *         playing a character on this server, such as if they are offline, have not yet picked a
	 *         character, or are still loading their selected character.
	 */
	PlayerCharacter getCurrentCharacter();

	boolean isCharacterLoaded();

	/**
	 * Gets the number of characters the player has.
	 * 
	 * @return The number of player-characters.
	 */
	int size();

	/**
	 * Gets a copy of the set of characters the player has.
	 * 
	 * @return The player's characters. An empty set if the player has none.
	 */
	Set<PlayerCharacter> getCharacterSet();

	/**
	 * Removes a player's character from their available list.
	 * 
	 * @return If the removal was successful
	 */
	boolean removeCharacter(int characterNumber);

	/**
	 * Gets the player's character with a given number.
	 * 
	 * @param characterNumber The number of the character to get (an identified unique only within the
	 *        player's characters).
	 * @return The player's character with the given number, if one is found. Else <code>null</code>.
	 */
	PlayerCharacter getForNumber(int characterNumber);

	/**
	 * Gets the player's character for a given name.
	 * 
	 * @param id The unique name of the character to get.
	 * @return The player's character with the given name, if one is found <b>for this player
	 *         specifically</b>. Else <code>null</code>.
	 */
	PlayerCharacter getForId(CharacterId id);

}
