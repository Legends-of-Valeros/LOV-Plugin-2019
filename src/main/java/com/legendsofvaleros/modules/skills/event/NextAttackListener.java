package com.legendsofvaleros.modules.skills.event;

import com.legendsofvaleros.modules.combatengine.events.CombatEnginePhysicalDamageEvent;

@FunctionalInterface
public interface NextAttackListener {
	void run(CombatEnginePhysicalDamageEvent event);
}