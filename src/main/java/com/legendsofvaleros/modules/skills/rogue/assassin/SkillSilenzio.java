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

public class SkillSilenzio extends Skill {
	public static final String ID = "silzenzio";
	private static final int[] LEVELS = new int[] { 5 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 40 };
	private static final int[] TIME = new int[] {  };
	private static final Object[] DESCRIPTION = new Object[] {
			"Silences a target for ", new TimePart().seconds(TIME), "."
		};

	public SkillSilenzio() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Silenzio"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: Do.
		return true;
	}
}