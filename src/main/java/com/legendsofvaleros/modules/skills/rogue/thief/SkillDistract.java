package com.legendsofvaleros.modules.skills.rogue.thief;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillDistract extends Skill {
	public static final String ID = "distract";
	private static final int[] LEVELS = new int[] { 2 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 120 };
	private static final int[] RANGE = new int[] { 20 };
	private static final int[] RADIUS = new int[] { 10 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Throws a decoy up to ", new RangePart(RANGE), " to lure monsters in a ", new RadiusPart(RADIUS), "."
		};

	public SkillDistract() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Distract"; }

	@Override
	public String getActivationTime() { return NONE; }
	
	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		return true;
	}
}