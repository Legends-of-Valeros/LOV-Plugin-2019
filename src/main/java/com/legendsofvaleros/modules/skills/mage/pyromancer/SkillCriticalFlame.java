package com.legendsofvaleros.modules.skills.mage.pyromancer;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder.ModifierType;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.skills.event.ParticleFollow;
import org.bukkit.Particle;
import org.bukkit.World;

public class SkillCriticalFlame extends Skill {
	public static final String ID = "criticalflame";
	private static final int[] LEVELS = new int[] { 6, 1, 2 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] CRIT_CHANCE = new int[] { 15, 20, 25 };
	private static final int[] MANA_REGEN = new int[] { 5 };
	private static final int[] TIME = new int[] { 10 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Your spells have ", CRIT_CHANCE, "% more chance to do critical damage for ",
			new TimePart().seconds(TIME), ", mana regen ", MANA_REGEN, "%/s."
		};

	public SkillCriticalFlame() { super(ID, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Critical Flame"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean doesRequireFocus() { return true; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		world.playSound(ce.getLivingEntity().getLocation(), "spell.fire.flame.scary.long", 1F, 1F);
		
		ParticleFollow.follow(ce.getLivingEntity(), getEarliest(TIME, level) * 20, Particle.FLAME, 2, .2, 0, .2);
		EntityStats stats = CombatEngine.getEntity(ce.getLivingEntity()).getStats();
		stats.newStatModifierBuilder(Stat.CRIT_CHANCE)
				.setDuration(getEarliest(TIME, level) * 20)
				.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT_IGNORES_MULTIPLIERS)
				.setValue(getEarliest(CRIT_CHANCE, level))
				.build();
		stats.newStatModifierBuilder(Stat.MANA_REGEN)
				.setDuration(getEarliest(TIME, level) * 20)
				.setModifierType(ValueModifierBuilder.ModifierType.FLAT_EDIT_IGNORES_MULTIPLIERS)
				.setValue(getEarliest(MANA_REGEN, level))
				.build();
		return true;
	}
}