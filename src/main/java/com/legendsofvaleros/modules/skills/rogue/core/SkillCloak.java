package com.legendsofvaleros.modules.skills.rogue.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillCloak extends Skill {
	public static final String ID = "cloak";
	private static final int[] LEVELS = new int[] { -1 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] TIME = new int[] { 30 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Toggles sneaking, invisibility for ", new TimePart().seconds(TIME), ", cancelled by attacking or taking damage."
		};

	public SkillCloak() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Cloak"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}