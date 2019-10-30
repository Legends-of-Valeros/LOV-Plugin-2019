package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.modules.classes.stats.AbilityStat;

public interface AbilityStatInformationConfig {

	/**
	 * Get the provided description information for a defined stat.
	 * 
	 * @return A description of the stat.
	 */
	String getStatDescription(AbilityStat stat);
}