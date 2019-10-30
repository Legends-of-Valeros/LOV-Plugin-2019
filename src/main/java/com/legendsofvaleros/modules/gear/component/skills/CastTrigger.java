package com.legendsofvaleros.modules.gear.component.skills;

import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.gear.trigger.CombatEntityTrigger;

public class CastTrigger extends CombatEntityTrigger {
	public final Skill skill;
	public final int level;
	
	public CastTrigger(CombatEntity caster, Skill skill, int level) {
		super(caster);
		
		this.skill = skill;
		this.level = level;
	}
}