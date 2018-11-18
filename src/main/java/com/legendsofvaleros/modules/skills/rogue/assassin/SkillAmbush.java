package com.legendsofvaleros.modules.skills.rogue.assassin;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillAmbush extends Skill {
	public static final String ID = "ambush";
	private static final int[] LEVELS = new int[] { 4, 2 };
	private static final int[] COST = new int[] { 15 };
	private static final double[] COOLDOWN = new double[] { 120 };
	private static final int[] DAMAGE = new int[] { 325, 350 };
	private static final int[] TIME = new int[] { 2, 3 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Does ", new WDPart(DAMAGE), " and dazes the target for ", new TimePart().seconds(TIME), "."
		};

	public SkillAmbush() { super(ID, EntityClass.ROGUE, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Ambush"; }

	@Override
	public String getActivationTime() { return NONE; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		/*NextAttack.on(pc.getPlayer().getUniqueId(), new NextAttackListener() {
			@Override
			public double run(Player p, LivingEntity attacked, double dmg) {
				Characters.inst().getSkillEffectManager().getSkillEffect("Confuse").apply(attacked, p, level, getEarliest(TIME, level) * 1000);
				return dmg * (getEarliest(DAMAGE, level) / 100D);
			}
		});*/
		return true;
	}
}