package com.legendsofvaleros.modules.skills.warrior.guardian;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillExecute extends Skill {
	public static final String ID = "execute";
	private static final int[] LEVELS = new int[] { 5, 1, 1, 1, 2 };
	private static final int[] COST = new int[] { 10 };
	private static final double[] COOLDOWN = new double[] { 50 };
	private static final int[] DAMAGE = new int[] { 300, 325, 350, 375, 400 };
	private static final int[] HEAL = new int[] { 50, 55, 60, 65, 80 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Does ", DAMAGE, "% of the weapon damage and heals the warrior by ", HEAL, "% of the damage dealt."
		};

	public SkillExecute() { super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Execute"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> {
			e.setRawDamage(e.getRawDamage() * getEarliest(DAMAGE, level) / 100D);
			ce.getStats().editRegeneratingStat(RegeneratingStat.HEALTH, e.getFinalDamage() * (getEarliest(HEAL, level) / 100D));
		});
		return true;
	}
}