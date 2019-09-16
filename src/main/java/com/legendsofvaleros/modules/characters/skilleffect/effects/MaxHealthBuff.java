package com.legendsofvaleros.modules.characters.skilleffect.effects;

import com.legendsofvaleros.util.RomanNumeral;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.skilleffect.*;
import com.legendsofvaleros.modules.characters.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifier;
import com.legendsofvaleros.modules.combatengine.modifiers.ValueModifierBuilder;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Buffs maximum health.
 * <p>
 * Each buff is pseudorandomly varied from a base value based on the level.
 */
public class MaxHealthBuff extends SkillEffect<Double> {

	public static final double PERCENT_PER_LEVEL = 0.01;
	public static final long MILLIS_PER_LEVEL = 60000;

	private static final String BASE_UI_NAME = "Max Health Buff";

	private Map<UUID, ValueModifier> mods;

	public MaxHealthBuff() throws IllegalArgumentException {
		super("MaxHealthBuff", 1, Integer.MAX_VALUE, true, InterruptionCause.DEATH);
		mods = new HashMap<>();
	}

	@Override
	public String generateUserFriendlyName(MetaEffectInstance<Double> effectInstance) {
		String ret = BASE_UI_NAME;
		if (effectInstance != null && effectInstance.getLevel() > 1) {
			ret += " " + RomanNumeral.convertToRoman(effectInstance.getLevel());
		}
		return ret;
	}

	@Override
	public String generateUserFriendlyDetails(MetaEffectInstance<Double> effectInstance) {
		if (effectInstance != null) {
			return "Increases max health by " + (Math.round(100 * effectInstance.getMeta()) - 100) + "%";

		} else {
			return "Increases max health by a percentage.";
		}
	}

	@Override
	protected long getDefaultDurationMillis(LivingEntity affected, int effectLevel) {
		return MILLIS_PER_LEVEL * effectLevel;
	}

	@Override
	protected void onRemove(UUID entityId, LivingEntity removeFrom,
			MetaEffectInstance<Double> effectInstance, RemovalReason reason) {
		ValueModifier mod = mods.get(effectInstance.getAffectedId());
		if (mod != null) {
			mod.remove();
		}
	}

	@Override
	protected boolean onApply(LivingEntity applyTo, MetaEffectInstance<Double> effectInstance,
			MetaEffectInstance<Double> replaced, long durationMillis) {

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

		double multiplier = getPercentageBoost(effectInstance.getLevel());
		// stores the per-player-character multiplier in the meta
		effectInstance.setMeta(multiplier);

		ValueModifier mod =
				ce.getStats().newStatModifierBuilder(Stat.MAX_HEALTH).setValue(multiplier)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER).build();
		mods.put(applyTo.getUniqueId(), mod);
		return true;
	}

	@Override
	protected boolean onPersist(PlayerCharacter persistingFor,
			MetaEffectInstance<Double> effectInstance, PersistingEffectBuilder builder) {
		// stores the per-player-character meta in the persistent record so it will be the same when the
		// player logs back in
		builder.setStringMeta(String.valueOf(effectInstance.getMeta()));
		return true;
	}

	@Override
	protected boolean onReapply(PlayerCharacter reapplyTo, MetaEffectInstance<Double> effectInstance,
			PersistingEffect persistedRecord) {
		// gets the per-player-character multiplier from the meta
		double multiplier = Double.valueOf(persistedRecord.getStringMeta());
		// puts it back in the local object's meta after converting it to its actual type
		effectInstance.setMeta(multiplier);

		CombatEntity ce = CombatEngine.getEntity(reapplyTo.getPlayer());

		ValueModifier mod =
				ce.getStats().newStatModifierBuilder(Stat.MAX_HEALTH).setValue(multiplier)
				.setModifierType(ValueModifierBuilder.ModifierType.MULTIPLIER).build();
		mods.put(reapplyTo.getUniqueCharacterId().getPlayerId(), mod);
		return true;
	}

	private double getPercentageBoost(int level) {
		double boost = level * PERCENT_PER_LEVEL;
		// varies by between +25% and -25% of base value
		boost *= 0.75 + ThreadLocalRandom.current().nextDouble(0.5);
		return 1 + boost;
	}

}
