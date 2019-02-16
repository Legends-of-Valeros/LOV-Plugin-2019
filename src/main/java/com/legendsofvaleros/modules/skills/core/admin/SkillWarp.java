package com.legendsofvaleros.modules.skills.core.admin;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillWarp extends Skill {
	public static final String ID = "admin-warp";
	private static final double[] COOLDOWN = new double[] { 5 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Teleport to a place in the world."
		};

	public SkillWarp() { super(ID, Type.SELF, EntityClass.MAGE, null, null, COOLDOWN, DESCRIPTION); }
	
	@Override
	public String getUserFriendlyName(int level) { return "Warp"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		return true;
	}
}