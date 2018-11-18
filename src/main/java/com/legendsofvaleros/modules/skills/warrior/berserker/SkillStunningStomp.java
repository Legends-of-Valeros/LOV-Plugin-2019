package com.legendsofvaleros.modules.skills.warrior.berserker;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
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
			new RadiusPart(RADIUS), " by ", SPEED, "% for ",
			new TimePart().seconds(TIME), "."
		};

	public SkillStunningStomp() { super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Stunning Stomp"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Collection<LivingEntity> targets = getTargets(ce, getEarliest(RADIUS, level), LivingEntity.class);
		for(LivingEntity entity : targets) {
			if(entity != ce.getLivingEntity())
				CombatEngine.getEntity(entity)
					.getStats().newStatModifierBuilder(Stat.SPEED)
						.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
						.setValue(1 - getEarliest(SPEED, level) / 100D)
						.setDuration(getEarliest(TIME, level) * 20)
						.setRemovedOnDeath(true)
						.build();
		}
		
		return targets.size() != 0;
	}
}