package com.legendsofvaleros.modules.characters.api;

import com.legendsofvaleros.modules.characters.core.InventoryData;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.skill.SkillSet;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A character of a player.
 * <p>
 * A character is an instance of a profile for a player. It contains its own distinct progression,
 * inventory, class, race, etc. in order for players to have multiple, separate instances of these
 * things within the same Minecraft account.
 */
public interface PlayerCharacter {
	/**
	 * @return If this player character is controlled by an NPC.
	 */
	boolean isNPC();

	/**
	 * Gets a unique string identifier for this character.
	 * 
	 * @return This character's unique name.
	 */
	CharacterId getUniqueCharacterId();

	/**
	 * Gets the number of this character, unique only to its player.
	 * 
	 * @return This character's number where a player's first character is <code>1</code>.
	 */
	int getCharacterNumber();

	/**
	 * Gets the name of the player that this character is for.
	 * 
	 * @return The player that can play this character.
	 */
	UUID getPlayerId();

	/**
	 * Gets the player that this character is for, if they are still in memory.
	 * 
	 * @return The player this character is for, if they are in memory. Else <code>null</code>.
	 */
	Player getPlayer();

	/**
	 * Gets whether this player-character is currently being played by the player.
	 * 
	 * @return <code>true</code> if this player-character object is currently in use, else
	 *         <code>false</code>.
	 */
	boolean isCurrent();

	/**
	 * Gets the current location of this player-character.
	 * <p>
	 * If the character is not being played right now, then the location that the player would log in
	 * at when they switched to this character.
	 * 
	 * @return This player-character's location.
	 */
	Location getLocation();

	/**
	 * Gets this player-character's race (ex: elf vs. human).
	 * 
	 * @return This player-character's race.
	 */
	EntityRace getPlayerRace();

	/**
	 * Gets this player-character's gameplay class.
	 * 
	 * @return This player-character's gameplay class.
	 */
	EntityClass getPlayerClass();

	/**
	 * Gets this player-character's listener amount and level.
	 * 
	 * @return This player-character's listener.
	 */
	Experience getExperience();

	/**
	 * Gets this player-character's cooldowns.
	 * 
	 * @return This player-character's cooldowns.
	 */
	Cooldowns getCooldowns();

	/**
	 * If this character is currently being played, gets this player-character's stats that have
	 * different effects depending on its player-class.
	 * <p>
	 * Class stats change across character logins/logouts and it is not safe to cache this object over
	 * periods of time.
	 * <p>
	 * To get whether a player-character is currently used, use {@link #isCurrent()}.
	 * 
	 * @return This player-character's class stats. <code>null</code> if this player-character is not
	 *         in use.
	 */
	AbilityStats getAbilityStats();

	/**
	 * Class stats change across character logins/logouts and it is not safe to cache this object over
	 * periods of time.
	 * 
	 * @return This player-character's inventory information.
	 */
	InventoryData getInventoryData();

	/**
	 * The class' skill set. This class handles most of the player's skill usage.
	 * 
	 * @return This player-character's skill set.
	 */
	SkillSet getSkillSet();
}
