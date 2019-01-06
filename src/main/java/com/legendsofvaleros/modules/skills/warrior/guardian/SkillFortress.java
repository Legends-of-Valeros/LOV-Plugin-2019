package com.legendsofvaleros.modules.skills.warrior.guardian;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.World;

public class SkillFortress extends Skill {
	public static final String ID = "fortress";
	private static final int[] LEVELS = new int[] { 6 };
	private static final int[] COST = new int[] { 0 };
	private static final double[] COOLDOWN = new double[] { 300 };
	private static final int[] INVINCIBLE_LEVEL = new int[] { 5 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Guardian receives ",
			new EffectPart<Void>("Invincible") {
				public void meta(int level, MetaEffectInstance<Void> meta) {
					meta.level = getEarliest(INVINCIBLE_LEVEL, level);
				}
			}, "."
		};

	public SkillFortress() { super(ID, Type.BENEFICIAL, EntityClass.WARRIOR, LEVELS, COST, COOLDOWN, DESCRIPTION); }

	@Override
	public String getUserFriendlyName(int level) { return "Fortress"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		Characters.getInstance().getSkillEffectManager().getSkillEffect("Invincible").apply(ce.getLivingEntity(), ce.getLivingEntity(), getEarliest(INVINCIBLE_LEVEL, level));
		return true;
	}
}