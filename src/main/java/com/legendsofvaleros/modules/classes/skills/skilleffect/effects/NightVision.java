package com.legendsofvaleros.modules.classes.skills.skilleffect.effects;

import com.legendsofvaleros.util.RomanNumeral;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.classes.skills.skilleffect.*;
import com.legendsofvaleros.modules.classes.skills.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Makes an entity unable to move.
 */
public class NightVision extends SkillEffect<Double> {
	private static final long MILLIS_PER_LEVEL = 60000;

	private static final String BASE_UI_NAME = "Night Vision";

	public NightVision() throws IllegalArgumentException {
		super("NightVision", 1, Integer.MAX_VALUE, true, InterruptionCause.DEATH);
	}

	@Override
	public String generateUserFriendlyName(MetaEffectInstance<Double> effectInstance) {
		String ret = BASE_UI_NAME;
		if(effectInstance != null && effectInstance.getLevel() > 1)
			ret += " " + RomanNumeral.convertToRoman(effectInstance.getLevel());
		return ret;
	}

	@Override
	public String generateUserFriendlyDetails(MetaEffectInstance<Double> effectInstance) {
		if(effectInstance != null) {
			return "Gain night vision for " + (effectInstance.getLevel() * MILLIS_PER_LEVEL / 1000) + " seconds.";
		}else
			return "See in the dark.";
	}

	@Override
	protected long getDefaultDurationMillis(LivingEntity affected, int effectLevel) {
		return MILLIS_PER_LEVEL * effectLevel;
	}

	@Override
	protected void onRemove(UUID entityId, LivingEntity removeFrom, MetaEffectInstance<Double> effectInstance, RemovalReason reason) {
		CombatEntity ce = CombatEngine.getEntity(removeFrom);
		com.legendsofvaleros.modules.combatengine.statuseffects.NightVision.remove(ce);
	}

	@Override
	protected boolean onApply(LivingEntity applyTo, MetaEffectInstance<Double> effectInstance, MetaEffectInstance<Double> replaced, long durationMillis) {
		if (replaced != null) {
			// does not overwrite a previous instance if the new instance is lower level or is the same
			// level but has a shorter duration
			if (effectInstance.getLevel() < replaced.getLevel()
					|| (effectInstance.getLevel() == replaced.getLevel() && effectInstance
					.getRemainingDurationMillis() <= replaced.getRemainingDurationMillis())) {
				return false;
			}

			onRemove(applyTo.getUniqueId(), applyTo, replaced, RemovalReason.REPLACED);
		}

		CombatEntity ce = CombatEngine.getEntity(applyTo);
		com.legendsofvaleros.modules.combatengine.statuseffects.NightVision.apply(ce);
		return true;
	}

	@Override
	protected boolean onPersist(PlayerCharacter persistingFor, MetaEffectInstance<Double> effectInstance, PersistingEffectBuilder builder) {
		return true;
	}

	@Override
	protected boolean onReapply(PlayerCharacter reapplyTo, MetaEffectInstance<Double> effectInstance, PersistingEffect persistedRecord) {
		CombatEntity ce = CombatEngine.getEntity(reapplyTo.getPlayer());
		com.legendsofvaleros.modules.combatengine.statuseffects.NightVision.apply(ce);
		return true;
	}
}