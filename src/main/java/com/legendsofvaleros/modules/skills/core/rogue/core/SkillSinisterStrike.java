package com.legendsofvaleros.modules.skills.core.rogue.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillSinisterStrike extends Skill {
	public static final String ID = "sinisterstrike";
	private static final int[] LEVELS = new int[] { -1, 1, 1, 1, 1 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 10 };
	private static final int[] DAMAGE = new int[] { 130, 150, 180, 210, 250 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Strike that does ", new WDPart(DAMAGE), "."
		};

	public SkillSinisterStrike() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Sinister Strike"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		/*NextAttack.on(pc.getPlayer().getUniqueId(), new NextAttackListener() {
			@Override
			public double run(Player p, LivingEntity attacked, double dmg) {
				return dmg * (getEarliest(DAMAGE, level) / 100D);
			}
		});*/
		return true;
	}
}