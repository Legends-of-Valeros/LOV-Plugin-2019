package com.legendsofvaleros.modules.skills.rogue.assassin;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;

public class SkillWound extends Skill {
	public static final String ID = "wound";
	private static final int[] LEVELS = new int[] { 4, 1 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 120 };
	private static final int[] DAMAGE = new int[] { 50, 100 };
	private static final int[] HEALING = new int[] { 50, 55 };
	private static final int[] TIME = new int[] { 10 };
	private static final Object[] DESCRIPTION = new Object[] {
			new WDPart(DAMAGE), " wounds a target denying his healing ability by ",
			HEALING, "% for ", new TimePart().seconds(TIME), "."
		};

	public SkillWound() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Wound"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}