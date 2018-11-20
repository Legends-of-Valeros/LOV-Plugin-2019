package com.legendsofvaleros.modules.gear.component.trigger;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.gear.item.GearItem;

public class CombineTrigger extends CombatEntityTrigger {
	private final GearItem.Instance base;
	public GearItem.Instance getBase() { return base; }
	
	private final GearItem.Instance agent;
	public GearItem.Instance getAgent() { return agent; }
	
	public CombineTrigger(CombatEntity ce, GearItem.Instance base, GearItem.Instance agent) {
		super(ce);
		
		this.base = base;
		this.agent = agent;
	}
}