package com.legendsofvaleros.modules.combatengine.config;

import com.legendsofvaleros.modules.combatengine.stat.Stat;

public interface StatInformationConfig {

	/**
	 * Get the provided description information for a defined stat.
	 * 
	 * @return A description of the stat.
	 */
	String getStatDescription(Stat stat);
}