package com.legendsofvaleros.modules.skills.core.warrior.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillPummel extends Skill {
	public static final String ID = "pummel";
	private static final int[] LEVELS = new int[] { -1 };
	private static final int[] COST = new int[] { 15 };
	private static final double[] COOLDOWN = new double[] { 15 };
	private static final double[] SILENCE = new double[] { 4 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Pummels the target interrupting spellcasting and silencing them for ", SILENCE, " seconds."
		};
	
	public SkillPummel() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Pummel"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> Characters.getInstance().getSkillEffectManager().getSkillEffect("Silence").apply(e.getDamaged().getLivingEntity(), ce.getLivingEntity(), level, (long)(getEarliest(SILENCE, level) * 1000L)));
		return true;
	}
}