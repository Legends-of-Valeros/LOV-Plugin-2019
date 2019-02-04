package com.legendsofvaleros.modules.gear.trigger;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.gear.item.Gear;

public class CombineTrigger extends CombatEntityTrigger {
	private final Gear.Instance base;
	public Gear.Instance getBase() { return base; }
	
	private final Gear.Instance agent;
	public Gear.Instance getAgent() { return agent; }
	
	public CombineTrigger(CombatEntity ce, Gear.Instance base, Gear.Instance agent) {
		super(ce);
		
		this.base = base;
		this.agent = agent;
	}
}