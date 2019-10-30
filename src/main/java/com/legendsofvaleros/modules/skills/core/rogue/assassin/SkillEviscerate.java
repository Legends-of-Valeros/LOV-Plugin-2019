package com.legendsofvaleros.modules.skills.core.rogue.assassin;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillEviscerate extends Skill {
	public static final String ID = "eviscerate";
	private static final int[] LEVELS = new int[] { 5, 1, 2 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] DAMAGE = new int[] { 200, 250, 300 };
	private static final int[] BYPASS = new int[] { 15, 17, 19 };
	private static final int[] RANGE = new int[] { 15, 17, 19 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Does ", new WDPart(DAMAGE), " within a ", RANGE, " block range, bypassing armor by ", new PercentPart(BYPASS), "."
		};

	public SkillEviscerate() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Eviscerate"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: do.
		return true;
	}
}