package com.legendsofvaleros.modules.skills.core.warrior.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillExecution extends Skill {
	public static final String ID = "execution";
	private static final int[] LEVELS = new int[] { -1, 1, 1, 1, 2 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 45 };
	private static final int[] DAMAGE = new int[] { 250, 260, 275, 300, 350 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Your next attack attempts to finish off the opponent, doing ", DAMAGE,
			"% physical damage. Only succeeds if enemy is below 20% health."
		};
	
	public SkillExecution() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Execution"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }
	
	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> {
			if(e.getDamaged().getStats().getRegeneratingStat(RegeneratingStat.HEALTH) / e.getDamaged().getStats().getStat(Stat.MAX_HEALTH) <= .2F)
				e.newDamageModifierBuilder("Execution")
							.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
							.setValue(getEarliest(DAMAGE, level) / 100D)
						.build();;
		});
		return true;
	}
}