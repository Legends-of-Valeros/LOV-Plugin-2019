package com.legendsofvaleros.modules.skills.core.mage.cryomancer;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import org.bukkit.Particle;
import org.bukkit.World;

public class SkillCooling extends Skill {
	public static final String ID = "cooling";
	private static final int[] LEVELS = new int[] { 4, 1, 2 };
	private static final int[] COST = new int[] { 4 };
	private static final double[] COOLDOWN = new double[] { 120 };
	private static final int[] HEALTH = new int[] { 35, 40, 40 };
	private static final int[] MANA = new int[] { 20, 25, 25 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Tiny ice crystals heal the mage for ",
			HEALTH, "% of his total health, regenerate ", MANA, "% mana."
		};

	public SkillCooling() { super(ID, Type.SELF, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION); }
	
	@Override
	public String getUserFriendlyName(int level) { return "Cooling"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean doesRequireFocus() { return true; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		EntityStats stats = CombatEngine.getEntity(ce.getLivingEntity()).getStats();
		stats.editRegeneratingStat(RegeneratingStat.HEALTH, getEarliest(HEALTH, level) / 100F * stats.getRegeneratingStat(RegeneratingStat.HEALTH));
		stats.editRegeneratingStat(RegeneratingStat.MANA, getEarliest(MANA, level) / 100F * stats.getRegeneratingStat(RegeneratingStat.MANA));
		
		world.playSound(ce.getLivingEntity().getLocation(), "spell.ice.freeze", .5F, 2F);
		world.spawnParticle(Particle.SNOWBALL, ce.getLivingEntity().getLocation().clone().add(0, 1, 0), 15, .5, 1, .5, .01);
		return true;
	}
}