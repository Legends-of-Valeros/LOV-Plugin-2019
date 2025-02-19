package com.legendsofvaleros.modules.skills.core.warrior.berserker;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillTrueStrike extends Skill {
	public static final String ID = "truestrike";
	private static final int[] LEVELS = new int[] { 5, 2 };
	private static final int[] COST = new int[] { 25 };
	private static final double[] COOLDOWN = new double[] { 60 };
	private static final int[] DAMAGE = new int[] { 400, 450 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Deals ", new WDPart(DAMAGE), " once."
		};

	public SkillTrueStrike() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "True Strike"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100,
				(e) -> e.newDamageModifierBuilder("True Strike")
								.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
								.setValue(getEarliest(DAMAGE, level) / 100D)
							.build());
		return true;
	}
}