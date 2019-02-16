package com.legendsofvaleros.modules.skills.core.mage.core;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffect;
import com.legendsofvaleros.modules.characters.skilleffect.SkillEffectInstance;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import org.bukkit.Sound;
import org.bukkit.World;

public class SkillSpellTheft extends Skill {
	public static final String ID = "spelltheft";
	private static final int[] LEVELS = new int[] { -1, 1, 1, 1 };
	private static final int[] COST = new int[] { 2 };
	private static final double[] COOLDOWN = new double[] { 180, 160, 120, 100 };
	private static final Object[] DESCRIPTION = new Object[] {
			"Steals a beneficial buff from the targeted enemy."
		};

	public SkillSpellTheft() { super(ID, Type.HARMFUL, EntityClass.MAGE, LEVELS, COST, COOLDOWN, DESCRIPTION); }
	
	@Override
	public String getUserFriendlyName(int level) { return "Spell Theft"; }

	@Override
	public String getActivationTime() { return INSTANT; }

	@Override
	public boolean onSkillUse(World world, CombatEntity ce, int level) {
		CombatEntity target = validateTarget(ce, getTarget(ce, 16D));
		if(target == null) return false;

		for(SkillEffect<?> effect : Characters.getInstance().getSkillEffectManager().getActiveEffects(target.getLivingEntity())) {
			if(!effect.isGood()) continue;
			if(effect.isAffected(ce.getLivingEntity())) continue;
			
			SkillEffectInstance instance = effect.getEntityInstance(target.getLivingEntity());
			effect.remove(target.getLivingEntity());
			effect.apply(ce.getLivingEntity(), instance.getAppliedBy(), instance.getLevel(), instance.getRemainingDurationMillis());

			world.playSound(ce.getLivingEntity().getLocation(), Sound.ENTITY_ENDERMEN_SCREAM, .5F, .5F);
			
			break;
		}
		
		return true;
	}
}