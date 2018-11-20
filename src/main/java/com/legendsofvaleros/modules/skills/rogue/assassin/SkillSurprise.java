package com.legendsofvaleros.modules.skills.rogue.assassin;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillSurprise extends Skill {
	public static final String ID = "surprise";
	private static final int[] LEVELS = new int[] { 6, 1, 2 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] RANGE = new int[] { 20 };
	private static final int[] DAMAGE = new int[] { 250, 300, 350 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Teleports up to ", new RangePart(RANGE), " to a target, stabbing it from behind dealing ", new WDPart(DAMAGE), "."
		};

	public SkillSurprise() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Surprise"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}