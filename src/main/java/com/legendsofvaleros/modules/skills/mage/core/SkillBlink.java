package com.legendsofvaleros.modules.skills.mage.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;

public class SkillBlink extends Skill {
	public static final String ID = "blink";
	private static final int[] LEVELS = new int[] { -1, 1, 1 };
	private static final int[] COST = new int[] { 2 };
	private static final double[] COOLDOWN = new double[] { 25 };
	private static final int[] RANGE = new int[] { 10, 12, 15 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Blinks the mage forward ", new RangePart(RANGE),
			" or until they reach an obstacle; also removes any stun effects."
		};

	public SkillBlink() { super(ID, Type.SELF, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION); }
	
	@Override
	public String getUserFriendlyName(int level) { return "Blink"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Location blinkLocation = getLookTargetGround(ce, getEarliest(RANGE, level));
		
		if(blinkLocation != null && isSafe(world, blinkLocation)) {
			world.spawnParticle(Particle.VILLAGER_HAPPY, ce.getLivingEntity().getLocation(), 12, 1, 1, 1, .01);
			world.spawnParticle(Particle.VILLAGER_HAPPY, blinkLocation, 8, 1, 1, 1, .01);
			
			world.playSound(ce.getLivingEntity().getLocation(), "spell.buff.teleport", .35F, 1F);
			world.playSound(blinkLocation, "spell.buff.teleport", .5F, 1F);
			
			blinkLocation.setPitch(ce.getLivingEntity().getLocation().getPitch());
			blinkLocation.setYaw(ce.getLivingEntity().getLocation().getYaw());
			ce.getLivingEntity().teleport(blinkLocation);

			return true;
		}
		
		world.spawnParticle(Particle.VILLAGER_ANGRY, ce.getLivingEntity().getLocation(), 5, 1, 1, 1);
		
		return false;
	}
	
	private boolean isSafe(World world, Location loc) {
		return (world.getBlockAt(loc).getType() == Material.AIR
					&& world.getBlockAt(loc.clone().add(0, 1, 0)).getType() == Material.AIR);
	}
}