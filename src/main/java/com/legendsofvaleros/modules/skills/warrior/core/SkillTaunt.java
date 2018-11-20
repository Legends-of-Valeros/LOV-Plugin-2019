package com.legendsofvaleros.modules.skills.warrior.core;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SkillTaunt extends Skill {
	public static final String ID = "taunt";
	private static final int[] LEVELS = new int[] { -1, 1, 1 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 30 };
	private static final int[] RADIUS = new int[] { 7, 10, 15 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Draws attention of mobs to the warrior in a ", new RadiusPart(RADIUS), "."
		};
	
	public SkillTaunt() { super(ID, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Taunt"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Collection<Player> targets = getTargets(ce, getEarliest(RADIUS, level), Player.class);
		for(LivingEntity entity : targets) {
			CombatEntity ece = CombatEngine.getEntity(entity);
			if(ece == null) continue;
			if(ece.getThreat() == null) continue;
			ece.getThreat().editThreat(ce.getLivingEntity(), 100);
		}
		return targets.size() != 0;
	}
}