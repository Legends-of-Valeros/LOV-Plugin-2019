package com.legendsofvaleros.modules.skills.warrior.berserker;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillTrueStrike extends Skill {
	public static final String ID = "truestrike";
	private static final int[] LEVELS = new int[] { 5, 2 };
	private static final int[] COST = new int[] { 25 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] DAMAGE = new int[] { 400, 450 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Deals weapon damage ", DAMAGE, "% once."
		};

	public SkillTrueStrike() { super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "True Strike"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> e.setRawDamage(e.getRawDamage() * getEarliest(DAMAGE, level) / 100D));
		return true;
	}
}