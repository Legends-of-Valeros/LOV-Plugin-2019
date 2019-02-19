package com.legendsofvaleros.modules.skills.core.warrior.berserker;

import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.skills.event.NextAttack;
import org.bukkit.World;

public class SkillDeadwish extends Skill {
	public static final String ID = "deadwish";
	private static final int[] LEVELS = new int[] { 4, 1, 1, 1, 2 };
	private static final int[] COST = new int[] { 20 };
	private static final double[] COOLDOWN = new double[] { 45 };
	private static final int[] DAMAGE = new int[] { 300, 350, 375, 400, 450 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Next single strike does ", DAMAGE, "% damage."
		};

	public SkillDeadwish() { super(ID, Type.HARMFUL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Deadwish"; }

	@Override
	public String getActivationTime() { return NEXT_ATTACK; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		NextAttack.on(ce.getUniqueId(), 100, (e) -> e.newDamageModifierBuilder("Deadwish")
					.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER)
					.setValue(getEarliest(DAMAGE, level) / 100D)
				.build());
		return true;
	}
}