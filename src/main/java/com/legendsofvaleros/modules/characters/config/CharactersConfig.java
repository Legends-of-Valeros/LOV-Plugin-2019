package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;

import java.util.List;

/**
 * Main configuration for Characters.
 */
public interface CharactersConfig extends AbilityStatInformationConfig, LocationConfig,
											CombatLoggingConfig, ExperienceConfig {

	/**
	 * Gets a configuration for a player-character class.
	 * 
	 * @param playerClass The class to get the configuration for.
	 * @return The configuration for the given class, if one is found. Else <code>null</code>.
	 */
	ClassConfig getClassConfig(EntityClass playerClass);

	/**
	 * Gets a configuration for a player-character race.
	 * 
	 * @param playerRace The race to get the configuration for.
	 * @return The configuration for the given race, if one is found. Else <code>null</code>.
	 */
	RaceConfig getRaceConfig(EntityRace playerRace);

	List<String> getCreationStartText();
	String getCreationStartNPC();
	List<String> getCreationCreateText();

}
