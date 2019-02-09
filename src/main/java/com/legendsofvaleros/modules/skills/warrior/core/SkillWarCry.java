package com.legendsofvaleros.modules.skills.warrior.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SkillWarCry extends Skill {
	public static final String ID = "warcry";
	private static final int[] LEVELS = new int[] { -1, 1, 2, 3, 5 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 120 };
	private static final int[] TIME = new int[] { 15 };
	private static final int[] RADIUS = new int[] { 15 };
	private static final int[] DAMAGE = new int[] { 10, 12, 15, 19, 25 };
	private static final Object[] DESCRIPTION = new Object[] {
			"For ", new TimePart().seconds(TIME), ", all party members within a ",
			new RadiusPart(RADIUS), " do ", DAMAGE, "% increased melee damage."
		};
	
	public SkillWarCry() { super(ID, Type.BENEFICIAL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "War Cry"; }

	@Override
	public String getActivationTime() { return INSTANT; }
	
	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Collection<CombatEntity> targets = validateTargets(ce, getTargets(ce, getEarliest(RADIUS, level), Player.class));
		for(CombatEntity entity : targets) {
			entity.getStats().newStatModifierBuilder(Stat.PHYSICAL_ATTACK)
					.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
					.setValue((getEarliest(DAMAGE, level) / 100D) + 1)
					.setDuration(getEarliest(TIME, level) * 20L)
					.setRemovedOnDeath(true)
					.build();
		}
		
		return targets.size() != 0;
	}
}