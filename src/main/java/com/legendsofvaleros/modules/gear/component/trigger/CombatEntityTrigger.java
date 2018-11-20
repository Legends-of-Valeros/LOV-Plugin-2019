package com.legendsofvaleros.modules.gear.component.trigger;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public abstract class CombatEntityTrigger extends GearTrigger {
	public final CombatEntity entity;

	public CombatEntity getEntity() { return entity; }
	
	public CombatEntityTrigger(CombatEntity entity) {
		this.entity = entity;
	}
}