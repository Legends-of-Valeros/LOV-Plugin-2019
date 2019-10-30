package com.legendsofvaleros.modules.characters.ui;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.ui.loading.ProgressViewProvider;
import com.legendsofvaleros.modules.classes.skills.listener.SkillEffectListener;
import org.bukkit.entity.Player;

/**
 * Manages various user-interface tasks for Characters.
 */
public interface CharactersUiManager extends ProgressViewProvider {

	/**
	 * Opens the character selection interface for a player and keeps it open, forcing the player to
	 * select which of their characters they want to play.
	 * <p>
	 * Forces the player to keep the interface open in one form or another until they make a
	 * selection. When a selection is made and the listener is informed, the interface will be closed
	 * or moved to its next stage.
	 * 
	 * @param characters The characters that the player should choose from.
	 * @param listener The class to inform when the player makes a selection.
	 */
	void forceCharacterSelection(PlayerCharacters characters, CharacterSelectionListener listener);

	/**
	 * Opens the character selection interface for a player, prompting them to select which character
	 * they want to play.
	 * <p>
	 * Allows the player to close the interface if they wish.
	 * 
	 * @param characters The characters that the player should be able to choose from.
	 * @param listener The class to inform when the player makes a selection.
	 */
	void openCharacterSelection(PlayerCharacters characters, CharacterSelectionListener listener);

	/**
	 * Puts a player in the character creation dialogue, where they will be asked to pick basic info
	 * about a new character before it is created.
	 * 
	 * @param player The player to put into the character creation process.
	 * @param listener The class to inform when a player confirms their selection and is done.
	 */
	void startCharacterCreation(Player player, int number, CharacterCreationListener listener);

	/**
	 * Open the character creation screen.
	 * @param player
	 */
	void openCharacterCreation(Player player);
	
	/**
	 * Gets a player-interface to inform of changes in a player-character's class stats, if one should
	 * be informed.
	 * 
	 * @param playerCharacter The player-character about whom class-stat-change information will be
	 *        reported.
	 * @return A class to inform of changes in the player-character's class stats. <code>null</code>
	 *         if no class should currently be informed.
	 */
	AbilityStatChangeListener getAbilityStatInterface(PlayerCharacter playerCharacter);

	/**
	 * Gets a player-interface to inform of changes in a player-character's active skill/spell
	 * effects, if one should be informed.
	 * <p>
	 * Active character effects are distinct from CombatEngine status effects. The former are
	 * higher-level, logical constructs which could do any number of things. The latter are a few very
	 * simple on/off combat effects like stun, silence, and blindess.
	 * 
	 * @param playerCharacter The player-character about whom character-effect-change information will
	 *        be reported.
	 * @return A class to inform of changes in the player-character's active character effects.
	 */
	SkillEffectListener getCharacterEffectInterface(PlayerCharacter playerCharacter);
}
