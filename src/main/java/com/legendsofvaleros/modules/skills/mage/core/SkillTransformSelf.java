package com.legendsofvaleros.modules.skills.mage.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.MetaEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Sound;
import org.bukkit.World;

public class SkillTransformSelf extends Skill {
	public static final String ID = "transform";
	private static final int[] LEVELS = new int[] { -1 };
	private static final int[] COST = new int[] { 5 };
	private static final double[] COOLDOWN = new double[] { 300 };
	private static final int[] MORPH_LEVEL = new int[] { 30 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Apply ", new EffectPart<Double>("Polymorph") {
				public void meta(int level, MetaEffectInstance<Double> meta) {
					meta.level = getEarliest(MORPH_LEVEL, level);
				}
			}, " to the mage for ",
			new TimePart().seconds(MORPH_LEVEL), " until damage is taken."
		};

	public SkillTransformSelf() { super(ID, Type.SELF, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION); }
	
	@Override
	public String getUserFriendlyName(int level) { return "Transform Self"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		world.playSound(ce.getLivingEntity().getLocation(), "misc.resurrect", 1F, 1F);
		world.playSound(ce.getLivingEntity().getLocation(), Sound.ENTITY_SHEEP_AMBIENT, .5F, 1F);
		Characters.getInstance().getSkillEffectManager().getSkillEffect("Polymorph").apply(ce.getLivingEntity(), ce.getLivingEntity(), getEarliest(MORPH_LEVEL, level));
		return true;
	}
}