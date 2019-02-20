package com.legendsofvaleros.modules.skills.core.rogue.thief;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillFrighten extends Skill {
	public static final String ID = "frighten";
	private static final int[] LEVELS = new int[] { 3, 2 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 30 };
	private static final int[] DAMAGE = new int[] { 150, 250 };
	private static final int[] TIME = new int[] { 5 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Do ", new WDPart(DAMAGE), " and stun the enemy for up to ", new TimePart().seconds(TIME), "."
		};

	public SkillFrighten() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Frighten"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}