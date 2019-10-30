package com.legendsofvaleros.modules.skills.core.rogue.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillRoundhouseKick extends Skill {
	public static final String ID = "roundhousekick";
	private static final int[] LEVELS = new int[] { -1, 1, 2, 3, 4 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] DAMAGE = new int[] { 70, 100, 150, 200, 300 };
	private static final double[] TIME = new double[] { 2.5, 3, 4, 5, 6 };
	private static final int[] RANGE = new int[] { 3 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Deals ", new WDPart(DAMAGE), " to targets within ",
			new RangePart(RANGE), " and silences them for ",
			new TimePart().seconds(TIME), "."
		};

	public SkillRoundhouseKick() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Roundhouse Kick"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: do
		return false;
	}
}