package com.legendsofvaleros.modules.skills.core.warrior.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import org.bukkit.World;

public class SkillRefresh extends Skill {
	public static final String ID = "refresh";
	private static final int[] LEVELS = new int[] { -1, 1, 2 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 300, 270, 210 };
	private static final int[] RAGE = new int[] { 100 };
	private static final Object[] DESCRIPTION = new Object[] {
			"The warrior gains ", RAGE, " rage."
		};
	
	public SkillRefresh() { super(ID, Type.SELF, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Refresh"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		ce.getStats().editRegeneratingStat(RegeneratingStat.ENERGY, getEarliest(RAGE, level));
		return true;
	}
}