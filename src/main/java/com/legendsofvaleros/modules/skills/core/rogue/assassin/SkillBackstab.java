package com.legendsofvaleros.modules.skills.core.rogue.assassin;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillBackstab extends Skill {
	public static final String ID = "backstab";
	private static final int[] LEVELS = new int[] { 3, 1, 1, 2, 3 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 120 };
	private static final int[] DAMAGE = new int[] { 150, 200, 250, 300, 350 };
	private static final int[] DAMAGE_STEALTH = new int[] { 240, 300, 370, 450, 500 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Does ", new WDPart(DAMAGE), " when attacking from behind the target, ",
			new PercentPart(DAMAGE_STEALTH), " when from stealth"
		};

	public SkillBackstab() { super(ID, Type.HARMFUL, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Backstab"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		/*NextAttack.on(pc.getPlayer().getUniqueId(), new NextAttackListener() {
			@Override
			public double run(Player p, LivingEntity attacked, double dmg) {
				double angle = p.getLocation().getDirection().angle(attacked.getLocation().getDirection()) / 180 * Math.PI;
				p.sendMessage("Angle to Attacked: " + angle);
				if(angle < 180 && angle < 0)
					return dmg;
				return dmg * (getEarliest(hasSkillEffect(p, "Invisible") ? DAMAGE_STEALTH : DAMAGE, level) / 100D);
			}
		});*/
		return true;
	}
}