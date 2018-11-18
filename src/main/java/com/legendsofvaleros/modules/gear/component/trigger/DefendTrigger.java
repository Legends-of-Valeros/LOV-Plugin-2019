package com.legendsofvaleros.modules.gear.component.trigger;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;

public class DefendTrigger extends GearTrigger {
	public final CombatEngineDamageEvent event;

	public CombatEntity getAttacker() { return event.getAttacker(); }
	public CombatEntity getDefender() { return event.getDamaged(); }
	
	public DefendTrigger(CombatEngineDamageEvent event) {
		this.event = event;
	}
}