package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.modules.classes.stats.StatModifierModel;
import com.legendsofvaleros.modules.characters.race.EntityRace;

import java.util.Collection;
import java.util.List;

/**
 * Configuration options for a player-character class.
 */
public interface RaceConfig {

	/**
	 * Gets the starting items for the player class.
	 * 
	 * @return The starting items.
	 */
	List<String> getDescription();

	List<String> getClimateDescription();

	/**
	 * Gets the player-character class this configuration is for.
	 * 
	 * @return This player-character class this is a configuration for.
	 */
	EntityRace getPlayerRace();
	
	/**
	 * Gets the modifications to the base stats that each stat point gives per level.
	 * 
	 * @return The stat modifications for the ability stats in this player-character race.
	 */
	Collection<StatModifierModel> getModifiers();

}
