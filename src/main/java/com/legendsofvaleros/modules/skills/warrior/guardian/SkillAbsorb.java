package com.legendsofvaleros.modules.skills.warrior.guardian;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillAbsorb extends Skill {
	public static final String ID = "absorb";
	private static final int[] LEVELS = new int[] { 5, 2 };
	private static final int[] COST = new int[] { 35 };
	private static final double[] COOLDOWN = new double[] { 300 };
	private static final int[] RESIST = new int[] { 50, 55 };
	private static final int[] ABSORB = new int[] { 50, 55 };
	private static final int[] RANGE = new int[] { 15 };
	private static final int[] TIME = new int[] { 5 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Resist ", RESIST, "% of all damage, absorbs ", ABSORB,
			"% of all damage to party members in ", new RangePart(RANGE),
			" range for ", new TimePart().seconds(TIME), "."
		};

	public SkillAbsorb() { super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Absorb"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}