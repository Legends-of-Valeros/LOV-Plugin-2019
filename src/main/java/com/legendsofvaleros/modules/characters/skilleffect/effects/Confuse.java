package com.legendsofvaleros.modules.characters.skilleffect.effects;

import com.legendsofvaleros.util.RomanNumeral;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skilleffect.*;
import com.legendsofvaleros.modules.characters.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.statuseffects.Confusion;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Makes an entity unable to move.
 */
public class Confuse extends SkillEffect<Double> {
	public static final long MILLIS_PER_LEVEL = 1000;

	private static final String BASE_UI_NAME = "Confusion";

	public Confuse() throws IllegalArgumentException {
		super("Confusion", 1, Integer.MAX_VALUE, false, InterruptionCause.DEATH, InterruptionCause.TAKE_DAMAGE);
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
			return "Blind entity for " + (effectInstance.getLevel() * MILLIS_PER_LEVEL / 1000) + " seconds.";
		}else
			return "Confuse your enemies.";
	}

	@Override
	protected long getDefaultDurationMillis(LivingEntity affected, int effectLevel) {
		return MILLIS_PER_LEVEL * effectLevel;
	}

	@Override
	protected void onRemove(UUID entityId, LivingEntity removeFrom, MetaEffectInstance<Double> effectInstance, RemovalReason reason) {
		CombatEntity ce = CombatEngine.getEntity(removeFrom);
		Confusion.remove(ce);
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
		Confusion.apply(ce);
		return true;
	}

	@Override
	protected boolean onPersist(PlayerCharacter persistingFor, MetaEffectInstance<Double> effectInstance, PersistingEffectBuilder builder) {
		return true;
	}

	@Override
	protected boolean onReapply(PlayerCharacter reapplyTo, MetaEffectInstance<Double> effectInstance, PersistingEffect persistedRecord) {
		CombatEntity ce = CombatEngine.getEntity(reapplyTo.getPlayer());
		Confusion.apply(ce);
		return true;
	}
}