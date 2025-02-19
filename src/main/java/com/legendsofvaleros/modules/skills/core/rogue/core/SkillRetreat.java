package com.legendsofvaleros.modules.skills.core.rogue.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillRetreat extends Skill {
	public static final String ID = "retreat";
	private static final int[] LEVELS = new int[] { -1, 1, 2 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 75 };
	private static final int[] DISTANCE = new int[] { 10, 12, 15 };
	private static final Object[] DESCRIPTION = new Object[] {
			"The rogue jumps back ", DISTANCE, " blocks"
		};

	public SkillRetreat() { super(ID, Type.SELF, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Retreat"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}