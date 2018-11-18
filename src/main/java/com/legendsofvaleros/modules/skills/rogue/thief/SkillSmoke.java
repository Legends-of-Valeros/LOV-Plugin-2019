package com.legendsofvaleros.modules.skills.rogue.thief;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillSmoke extends Skill {
	public static final String ID = "smoke";
	private static final int[] LEVELS = new int[] { 6, 1, 1, 2 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 180 };
	private static final int[] TIME = new int[] { 5, 6, 7, 8 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Instant invisibility for ", new TimePart().seconds(TIME), ", damage received will not uncover you."
		};

	public SkillSmoke() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Smoke"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Characters.inst().getSkillEffectManager().getSkillEffect("Invisible").apply(ce.getLivingEntity(), ce.getLivingEntity(), getEarliest(TIME, level) * 1000);
		return true;
	}
}