package com.legendsofvaleros.modules.skills.core.warrior.berserker;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;

public class SkillEnrage extends Skill {
	public static final String ID = "enrage";
	private static final int[] LEVELS = new int[] { 6 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 300 };
	private static final int[] TIME = new int[] { 20 };
	private static final int[] DEFENCE = new int[] { 15 };
	private static final Object[] DESCRIPTION = new Object[] {
			"The warrior enrages for ", new TimePart().seconds(TIME), " increasing defense and damage by ", DEFENCE, "%."
		};

	public SkillEnrage() { super(ID, Type.BENEFICIAL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Enrage"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		ce.getStats().newStatModifierBuilder(Stat.ARMOR)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(getEarliest(DEFENCE, level) / 100D + 1D)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		ce.getStats().newStatModifierBuilder(Stat.PHYSICAL_ATTACK)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(getEarliest(DEFENCE, level) / 100D + 1D)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
	return true;
	}
}