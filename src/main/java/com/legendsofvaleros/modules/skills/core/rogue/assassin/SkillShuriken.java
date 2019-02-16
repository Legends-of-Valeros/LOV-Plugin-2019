package com.legendsofvaleros.modules.skills.core.rogue.assassin;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillShuriken extends Skill {
	public static final String ID = "shuriken";
	private static final int[] LEVELS = new int[] { 1, 1, 1, 2, 2 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 30 };
	private static final int[] DAMAGE = new int[] { 150, 200, 250, 300, 350 };
	private static final int[] RANGE = new int[] { 150, 200, 250, 300, 350 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Throw a shuriken up to ", new RangePart(RANGE), " dealing ", new WDPart(DAMAGE), "."
		};

	public SkillShuriken() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Shuriken"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}