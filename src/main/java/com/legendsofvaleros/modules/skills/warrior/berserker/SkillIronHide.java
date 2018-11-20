package com.legendsofvaleros.modules.skills.warrior.berserker;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;

public class SkillIronHide extends Skill {
	public static final String ID = "ironhide";
	private static final int[] LEVELS = new int[] { 4 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 240 };
	private static final int[] DEFENCE = new int[] { 25 };
	private static final int[] TIME = new int[] { 10 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Increases the warriors defense by ", DEFENCE, "% for ", new TimePart().seconds(TIME), "."
		};

	public SkillIronHide() { super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Iron Hide"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		ce.getStats().newStatModifierBuilder(Stat.ARMOR)
				.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT)
				.setValue(getEarliest(DEFENCE, level) / 100D + 1)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		return true;
	}
}