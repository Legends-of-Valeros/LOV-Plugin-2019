package com.legendsofvaleros.modules.skills.core.warrior.guardian;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;

public class SkillSunder extends Skill {
	public static final String ID = "sunder";
	private static final int[] LEVELS = new int[] { 2, 2 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 45 };
	private static final int[] DEFENCE = new int[] { 50, 55 };
	private static final int[] TIME = new int[] { 3, 4 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Decreases the targets defence by ", new PercentPart(DEFENCE), " for ", new TimePart().seconds(TIME), "."
		};

	public SkillSunder() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Sunder"; }

	@Override
	public String getActivationTime() { return TARGET; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		CombatEntity target = validateTarget(ce, getTarget(ce, 12));
		if(target == null) return false;
		
		target.getStats().newStatModifierBuilder(Stat.ARMOR)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
				.setValue(getEarliest(DEFENCE, level) / 100D)
				.setDuration(getEarliest(TIME, level) * 20)
				.setRemovedOnDeath(true)
				.build();
		return true;
	}
}