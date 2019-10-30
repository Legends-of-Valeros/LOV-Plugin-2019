package com.legendsofvaleros.modules.classes.skills.skilleffect;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.classes.skills.skilleffect.effects.*;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineStatusEffectAddedEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineStatusEffectRemovedEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates various types of timed effects caused by skills and spells.
 */
public class SkillEffects {

	private Map<String, SkillEffect<?>> effects;
	private Map<String, SkillEffect<?>> wrappedCombatEngineEffects;

	public SkillEffects() {
		effects = new HashMap<>();

		// initializes the different effects
		SkillEffect<?> ef;
		effects.put((ef = new MaxHealthBuff()).getId(), ef);
		effects.put((ef = new PercentagePoison()).getId(), ef);
		effects.put((ef = new Bleed()).getId(), ef);
		effects.put((ef = new Freeze()).getId(), ef);
		effects.put((ef = new Blind()).getId(), ef);
		effects.put((ef = new NightVision()).getId(), ef);
		effects.put((ef = new Confuse()).getId(), ef);
		effects.put((ef = new Invisible()).getId(), ef);
		effects.put((ef = new Polymorph()).getId(), ef);
		effects.put((ef = new Invincible()).getId(), ef);
		effects.put((ef = new Silence()).getId(), ef);

		// wraps each of the combatengine status effects in a wrapper that will let it persist across
		// player-character logins
		wrappedCombatEngineEffects = new HashMap<>();
		for (StatusEffectType type : StatusEffectType.values()) {
			wrappedCombatEngineEffects.put((ef = new PersistingStatusEffect(type)).getId(), ef);
		}

		Characters.getInstance().registerEvents(new StatusEffectListener());
	}

	/**
	 * Gets a skill effect for a given string name, if one is found.
	 * 
	 * @param effectId The name of the effect to get.
	 * @return The effect with the given name if one was found, else <code>null</code>.
	 */
	public SkillEffect<?> getSkillEffect(String effectId) {
		return effects.get(effectId);
	}

	/**
	 * Gets the skill/spell affects currently applied to a living entity.
	 * <p>
	 * For players, gets the affects for their currently logged-in player-character, if any.
	 * 
	 * @param getFor The entity to get active skill effects for.
	 * @return The entity's currently applied skill/spell effects. An empty set if no active effects
	 *         are found.
	 */
	public Set<SkillEffect<?>> getActiveEffects(LivingEntity getFor)
			throws IllegalArgumentException {
		Set<SkillEffect<?>> ret = new HashSet<>();

		for (SkillEffect<?> effect : effects.values()) {
			if (effect.isAffected(getFor)) {
				ret.add(effect);
			}
		}
		for (SkillEffect<?> effect : wrappedCombatEngineEffects.values()) {
			if (effect.isAffected(getFor)) {
				ret.add(effect);
			}
		}

		return ret;
	}

	/**
	 * Reapplies an effect that was stored persistently to apply across logins.
	 * 
	 * @param persistingEffect The persistent record of the effect.
	 * @throws IllegalArgumentException On a persistent effect that does not correlate to any of the
	 *         effects on this server.
	 */
	void reapplyEffect(PersistingEffect persistingEffect) throws IllegalArgumentException {
		SkillEffect<?> effect = effects.get(persistingEffect.getEffectId());
		if (effect == null) {
			effect = wrappedCombatEngineEffects.get(persistingEffect.getEffectId());
			if (effect == null) {
				throw new IllegalArgumentException("effect not found");
			}
		}
		effect.reapply(persistingEffect);
	}

	void registerEffect(SkillEffect<?> effect) throws IllegalArgumentException {
		if (effects.containsKey(effect.getId())) {
			throw new IllegalArgumentException("an effect with the name " + effect.getId()
			+ " already exists");
		}
		effects.put(effect.getId(), effect);
	}

	/**
	 * Automatically makes CombatEngine status effects (blindness, stun, polymorph, silence, etc.) on
	 * player-characters persist across logins.
	 * <p>
	 * Uses the existing persistent effect infrastructure and just listens for these effects being
	 * added and wraps them in skill-effect wrappers that can persist and also be used in
	 * user-interfaces that display skill effects.
	 */
	private class StatusEffectListener implements Listener {

		@EventHandler
		public void onCombatEngineStatusEffectAdded(CombatEngineStatusEffectAddedEvent event) {
			SkillEffect<?> effect =
					wrappedCombatEngineEffects.get(event.getEffectType().name());

			if (effect != null) {
				effect.apply(event.getAffectedEntity().getLivingEntity(), null, 1,
						(event.getDuration() * 50));
			}
		}

		@EventHandler
		public void onCombatEngineStatusEffectRemoved(CombatEngineStatusEffectRemovedEvent event) {
			SkillEffect<?> effect =
					wrappedCombatEngineEffects.get(event.getEffectType().name());

			if (effect != null) {
				StatusEffectType.RemovalReason statusReason = event.getRemovalReason();
				RemovalReason skillReason = null;

				switch (statusReason) {
				case EXPIRED:
					skillReason = RemovalReason.EXPIRED;
					break;
				case INTERRUPTED:
					skillReason = RemovalReason.INTERRUPTED;
				default:
					break;
				}

				// if combatengine gave a specific reason for the status effect being removed, uses that.
				// Else let's the skill effect decide.
				if (skillReason != null) {
					effect.remove(event.getAffectedEntity().getUniqueId(), event.getAffectedEntity()
							.getLivingEntity(), skillReason);

				} else {
					effect.remove(event.getAffectedEntity().getLivingEntity());
				}
			}
		}
	}

}
