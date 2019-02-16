package com.legendsofvaleros.modules.skills.core.rogue.thief;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;

public class SkillEvasiveMoves extends Skill {
	public static final String ID = "evasivemoves";
	private static final int[] LEVELS = new int[] { 5, 1, 1, 2, 3 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] DODGE = new int[] { 30, 33, 35, 40, 45 };
	private static final int[] TIME = new int[] { 6 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Increases your ability to dodge magical and physical attacks by ",
			DODGE, "% for ", new TimePart().seconds(TIME), "."
		};

	public SkillEvasiveMoves() { super(ID, Type.BENEFICIAL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Evasive Moves"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		ce.getStats().newStatModifierBuilder(Stat.DODGE_CHANCE)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(getEarliest(DODGE, level) / 100D + 1)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		return true;
	}
}