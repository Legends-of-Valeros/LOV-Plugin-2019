package com.legendsofvaleros.modules.skills.rogue.thief;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillShadowStrike extends Skill {
	public static final String ID = "shadowstrike";
	private static final int[] LEVELS = new int[] { 5, 1, 2, 3 };
	private static final int[] COST = new int[] { 15 };
	private static final double[] COOLDOWN = new double[] { 45 };
	private static final int[] DAMAGE = new int[] { 300, 400, 500, 600 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Attacks from stealth, dealing ", new WDPart(DAMAGE), "."
		};

	public SkillShadowStrike() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Shadow Strike"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}