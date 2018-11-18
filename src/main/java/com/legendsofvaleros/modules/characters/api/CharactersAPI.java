package com.legendsofvaleros.modules.characters.api;

import org.bukkit.entity.Player;

/**
 * Top-level interface for Characters.
 * <p>
 * Characters creates a framework for players to have multiple characters, each with their own
 * distinct progressions, stats, inventories, and other per-player data.
 */
public interface CharactersAPI {

	/**
	 * Gets the current character that a player is using. If it's an NPC, this returns null. Be prepared.
	 * 
	 * @param player The player whose current character to get.
	 * @return The player's current character. <code>null</code> if the player is not currently
	 *         playing a character on this server, such as if they are offline, have not yet picked a
	 *         character, are in the process of switching characters, or are still loading their
	 *         selected character.
	 */
	PlayerCharacter getCurrentCharacter(Player player);

	boolean isCharacterLoaded(Player player);

	/**
	 * Gets the player character object for a given unique character name, if it is found in memory.
	 * 
	 * @param uniqueId The unique character name of the character to get.
	 * @return The player character for the given unique name, if it is in memory. Else
	 *         <code>null</code>.
	 */
	PlayerCharacter getCharacter(CharacterId uniqueId);

	/**
	 * Gets the characters of a player.
	 * 
	 * @param player The player whose characters to get.
	 * @return The player's characters. <code>null</code> if no characters are found for the given
	 *         player, such as if the player is offline or is still loading their character data.
	 */
	PlayerCharacters getCharacters(Player player);

}
