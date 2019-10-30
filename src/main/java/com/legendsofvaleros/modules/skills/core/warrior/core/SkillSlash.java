package com.legendsofvaleros.modules.skills.core.warrior.core;

import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillSlash extends Skill {
	public static final String ID = "slash";
	private static final int[] LEVELS = new int[] { -1, 1, 1, 1, 2 };
	private static final int[] COST = new int[] { 20 };
	private static final double[] COOLDOWN = new double[] { 5 };
	private static final int[] DAMAGE = new int[] { 200, 210, 225, 240, 275 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Slashes the target, doing ", new WDPart(DAMAGE),  "."
		};
	
	public SkillSlash() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Slash"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, final int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> e.newDamageModifierBuilder("Slash")
					.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
					.setValue(getEarliest(DAMAGE, level) / 100D)
				.build());
		return true;
	}
}