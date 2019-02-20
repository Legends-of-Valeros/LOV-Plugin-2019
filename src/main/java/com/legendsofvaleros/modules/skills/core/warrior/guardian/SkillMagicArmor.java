package com.legendsofvaleros.modules.skills.core.warrior.guardian;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;

public class SkillMagicArmor extends Skill {
	public static final String ID = "magicarmor";
	private static final int[] LEVELS = new int[] { 2, 1, 2 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] SPELL_DEFENCE = new int[] { 50, 55, 60 };
	private static final int[] TIME = new int[] { 5 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Decreases spell damage received by ", SPELL_DEFENCE, "% for ", new TimePart().seconds(TIME), "."
		};

	public SkillMagicArmor() { super(ID, Type.BENEFICIAL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Magic Armor"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		ce.getStats().newStatModifierBuilder(Stat.FIRE_RESISTANCE)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(getEarliest(SPELL_DEFENCE, level) / 100D)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		ce.getStats().newStatModifierBuilder(Stat.ICE_RESISTANCE)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(getEarliest(SPELL_DEFENCE, level) / 100D)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		return true;
	}
}