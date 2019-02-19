package com.legendsofvaleros.modules.skills.core.rogue.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillRedFlask extends Skill {
	public static final String ID = "redflask";
	private static final int[] LEVELS = new int[] { -1, 1, 1, 1, 2 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] HEALTH = new int[] { 30, 31, 32, 33, 35 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Drink a potion to regain ", HEALTH, "% of your maximum health"
		};

	public SkillRedFlask() { super(ID, Type.BENEFICIAL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Red Flask"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}