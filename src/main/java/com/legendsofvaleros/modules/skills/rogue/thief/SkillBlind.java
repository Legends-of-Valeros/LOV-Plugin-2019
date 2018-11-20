package com.legendsofvaleros.modules.skills.rogue.thief;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.effects.Blind;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillBlind extends Skill {
	public static final String ID = "blind";
	private static final int[] LEVELS = new int[] { 5, 1 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 240 };
	private static final int[] RANGE = new int[] { 2, 3 };
	private static final int[] BLIND = new int[] { (int)((10 * 1000) / Blind.MILLIS_PER_LEVEL) };
	private static final Object[] DESCRIPTION = new Object[] {
			"Blinds the target within ", RANGE, " block range for ", BLIND, " seconds."
		};

	public SkillBlind() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Blind"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: do.
		return true;
	}
}