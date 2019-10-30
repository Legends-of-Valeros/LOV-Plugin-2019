package com.legendsofvaleros.modules.skills.core.warrior.berserker;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class SkillStunningStomp extends Skill {
	public static final String ID = "stunningstomp";
	private static final int[] LEVELS = new int[] { 3, 2, 3 };
	private static final int[] COST = new int[] { 15 };
	private static final double[] COOLDOWN = new double[] { 90 };
	private static final int[] RADIUS = new int[] { 3 };
	private static final int[] SPEED = new int[] { 25, 30, 35 };
	private static final int[] TIME = new int[] { 4, 5, 6 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Stomps the ground slowing all enemies in a ",
			new RadiusPart(RADIUS), " by ", new PercentPart(SPEED), " for ",
			new TimePart().seconds(TIME), "."
		};

	public SkillStunningStomp() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Stunning Stomp"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Collection<CombatEntity> targets = validateTargets(ce, getTargets(ce, getEarliest(RADIUS, level), LivingEntity.class));
		for(CombatEntity entity : targets) {
			entity.getStats().newStatModifierBuilder(Stat.SPEED)
					.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
					.setValue(1 - getEarliest(SPEED, level) / 100D)
					.setDuration(getEarliest(TIME, level) * 20)
					.setRemovedOnDeath(true)
					.build();
		}
		
		return targets.size() != 0;
	}
}