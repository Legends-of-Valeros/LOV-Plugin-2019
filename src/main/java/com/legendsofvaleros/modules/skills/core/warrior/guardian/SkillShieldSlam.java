package com.legendsofvaleros.modules.skills.core.warrior.guardian;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillShieldSlam extends Skill {
	public static final String ID = "shieldslam";
	private static final int[] LEVELS = new int[] { 4, 1, 1, 2, 3 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 180 };
	private static final int[] DAMAGE = new int[] { 400, 425, 450, 475, 500 };
	private static final int[] RAGE = new int[] { 20, 30, 40, 50, 75 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Slams target with shield, causing ", new WDPart(DAMAGE), ". Generates ", RAGE, " rage."
		};

	public SkillShieldSlam() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Shield Slam"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> {
			ce.getStats().editRegeneratingStat(RegeneratingStat.ENERGY, getEarliest(RAGE, level));
			e.newDamageModifierBuilder("Shield Slam")
						.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
						.setValue(getEarliest(DAMAGE, level) / 100D)
					.build();
		});
		return true;
	}
}