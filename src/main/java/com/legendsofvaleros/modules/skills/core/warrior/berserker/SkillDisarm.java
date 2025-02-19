package com.legendsofvaleros.modules.skills.core.warrior.berserker;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillDisarm extends Skill {
	public static final String ID = "disarm";
	private static final int[] LEVELS = new int[] { 3, 1, 3 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] TIME = new int[] { 5, 6, 8 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Disarms an enemy for ", new TimePart().seconds(TIME), "."
		};
	
	public SkillDisarm() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Diarm"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: DO.
		return false;
	}
}