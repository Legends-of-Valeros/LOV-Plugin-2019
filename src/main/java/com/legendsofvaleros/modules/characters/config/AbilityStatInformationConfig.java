package com.legendsofvaleros.modules.characters.config;

import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;
import com.legendsofvaleros.modules.characters.entityclass.AbilityStat;

public interface AbilityStatInformationConfig {

	/**
	 * Get the provided description information for a defined stat.
	 * 
	 * @return A description of the stat.
	 */
	String getStatDescription(AbilityStat stat);
}