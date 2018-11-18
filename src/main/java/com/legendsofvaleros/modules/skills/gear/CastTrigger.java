package com.legendsofvaleros.modules.skills.gear;

import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.gear.component.trigger.CombatEntityTrigger;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public class CastTrigger extends CombatEntityTrigger {
	public final Skill skill;
	public final int level;
	
	public CastTrigger(CombatEntity caster, Skill skill, int level) {
		super(caster);
		
		this.skill = skill;
		this.level = level;
	}
}