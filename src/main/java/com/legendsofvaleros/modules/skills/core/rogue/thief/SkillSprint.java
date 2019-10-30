package com.legendsofvaleros.modules.skills.core.rogue.thief;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;

public class SkillSprint extends Skill {
	public static final String ID = "sprint";
	private static final int[] LEVELS = new int[] { 2, 1, 1 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 30 };
	private static final int[] SPEED = new int[] { 50, 55, 60 };
	private static final int[] TIME = new int[] { 20 };
	private static final Object[] DESCRIPTION = new Object[] {
			"The thief moves ", new PercentPart(SPEED), " faster for ", new TimePart().seconds(TIME), "."
		};

	public SkillSprint() { super(ID, Type.SELF, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Sprint"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		ce.getStats().newStatModifierBuilder(Stat.SPEED)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue((getEarliest(SPEED, level) / 100D) + 1)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		return true;
	}
}