package com.legendsofvaleros.modules.skills.warrior.berserker;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.skills.event.OnTouchGround;
import com.legendsofvaleros.util.VelocityUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class SkillRush extends Skill {
	public static final String ID = "rush";
	private static final int[] LEVELS = new int[] { 4, 1, 3 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 30 };
	private static final int[] RANGE = new int[] { 15 };
	private static final int[] SPEED = new int[] { 50 };
	private static final int[] TIME = new int[] { 5, 6, 8 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Sprint forward for ", new RangePart(RANGE), " and slow down targets movement speed by ",
			SPEED, "% for ", new TimePart().seconds(TIME), "."
		};
	
	public SkillRush() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Rush"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Location to = ce.getLivingEntity().getLocation().clone();
		to.setPitch(0);
		to.add(to.getDirection().multiply(getEarliest(RANGE, level)));
		
		ce.getLivingEntity().setVelocity(VelocityUtil.calculateVelocity(VelocityUtil.PLAYER, ce.getLivingEntity().getLocation().toVector(), to.toVector(), 1));
		
		OnTouchGround.call(ce.getLivingEntity(), (le) -> {
			for(CombatEntity e : validateTargets(ce, getNearbyEntities(le.getLocation(), 3, 1, 3))) {
				CombatEngine.getEntity((LivingEntity)e)
					.getStats().newStatModifierBuilder(Stat.SPEED)
						.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
						.setValue(1 - getEarliest(SPEED, level) / 100D)
						.setDuration(getEarliest(TIME, level) * 20)
						.setRemovedOnDeath(true)
						.build();
			}
		});
		return true;
	}
}