package com.legendsofvaleros.modules.gear.trigger;

import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public abstract class AttackTrigger extends GearTrigger {
	protected final CombatEntity attacker;
	public CombatEntity getAttacker() { return attacker; }
	
	protected double damage = 0;
	public double getDamage() { return damage; }
	public void setDamage(double damage) { this.damage = damage; }
	
	public AttackTrigger(CombatEntity attacker) {
		this.attacker = attacker;
	}
}