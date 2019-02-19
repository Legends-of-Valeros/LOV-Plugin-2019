package com.legendsofvaleros.modules.skills.core.rogue.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillCheapShot extends Skill {
	public static final String ID = "cheapshot";
	private static final int[] LEVELS = new int[] { -1, 1, 2 };
	private static final int[] COST = new int[] { 25 };
	private static final double[] COOLDOWN = new double[] { 180 };
	private static final int[] DAMAGE = new int[] { 300, 350, 400 };
	private static final int[] TIME = new int[] { 2 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Only works when in stealth, ", new WDPart(DAMAGE), " stuns target for ", new TimePart().seconds(TIME), "."
		};

	public SkillCheapShot() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Cheap Shot"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		// TODO: add timeout
		/*if(hasSkillEffect(pc.getPlayer(), "Invisible")) {
			NextAttack.on(pc.getPlayer().getUniqueId(), new NextAttackListener() {
				@Override
				public double run(Player p, LivingEntity attacked, double dmg) {
					// Make sure they're still stealth'd
					return dmg * (hasSkillEffect(p, "Invisible") ? getEarliest(DAMAGE, level) / 100D : 1);
				}
			});
			return true;
		}*/
		
		return false;
	}
}