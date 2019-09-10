package com.legendsofvaleros.modules.characters.skilleffect;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stores and read data to/form a database for character effects that persist across logins.
 */
public class PersistingEffects {
	private interface RPC {
		Promise<Map<String, SavedPersistentEffect>> getPlayerPersistingEffects(CharacterId characterId);

		Promise<Object> savePlayerPersistingEffects(CharacterId characterId, Map<String, SavedPersistentEffect> effects);

		Promise<Boolean> deletePlayerPersistingEffects(CharacterId characterId);
	}

	private static class SavedPersistentEffect {
		public long remaining;
		public long elapsed;
		public int level;

		public String metaString;
		public byte[] metaBytes;

		private SavedPersistentEffect(PersistingEffect effect) {
			this.remaining = effect.getRemainingDurationMillis();
			this.elapsed = effect.getElapsedMillis();
			this.level = effect.getLevel();
			this.metaString = effect.getStringMeta();
			this.metaBytes = effect.getByteMeta();
		}
	}

	private static RPC rpc;

	private static final long MIN_MILLIS_REMAINING_TO_SAVE = 500;

	private static Multimap<CharacterId, PersistingEffect> dataMap;
	private static SkillEffects effectManager;

	public static void onEnable(SkillEffects characterEffects) {
		rpc = APIController.create(RPC.class);

		dataMap = HashMultimap.create();
		effectManager = characterEffects;

		Characters.getInstance().registerEvents(new PlayerCharacterListener());
	}

	/**
	 * Saves an effect to persist across logins.
	 * 
	 * @param effect The effect that should persist across logins.
	 * @see PersistingEffect
	 */
	static void saveEffect(PersistingEffect effect) {
		if (effect == null) {
			return;
		}

		dataMap.put(effect.getAffected(), effect);
	}

	private static Promise<Boolean> onLogin(PlayerCharacter pc) {
		Promise<Boolean> promise = new Promise<>();

		rpc.getPlayerPersistingEffects(pc.getUniqueCharacterId()).onSuccess(val -> {
			val.orElse(ImmutableMap.of()).forEach((id, saved) -> {
				PersistingEffectBuilder builder =
						PersistingEffect.newBuilder(id, pc.getUniqueCharacterId(), saved.remaining);

				builder.setElapsedDurationMillis(saved.elapsed);
				builder.setLevel(saved.level);
				builder.setStringMeta(saved.metaString);
				builder.setByteMeta(saved.metaBytes);

				dataMap.put(pc.getUniqueCharacterId(), builder.build());
			});

			promise.resolve(true);
		}).onFailure(promise::reject);

		return promise;
	}

	private static Promise onLogout(PlayerCharacter pc) {
		Map<String, SavedPersistentEffect> effects = new HashMap<>();

		dataMap.get(pc.getUniqueCharacterId()).stream()
				.filter(effect -> effect.getRemainingDurationMillis() >= MIN_MILLIS_REMAINING_TO_SAVE)
				.forEach(effect -> effects.put(effect.getEffectId(), new SavedPersistentEffect(effect)));

		return rpc.savePlayerPersistingEffects(pc.getUniqueCharacterId(), effects);
	}

	private static Promise onDelete(PlayerCharacter pc) {
		return rpc.deletePlayerPersistingEffects(pc.getUniqueCharacterId());
	}

	/**
	 * Loads and applies persisting effects on player-character logins.
	 */
	private static class PlayerCharacterListener implements Listener {
		@EventHandler
		public void onPlayerCharacterStartLoading(final PlayerCharacterStartLoadingEvent event) {
			PhaseLock lock = event.getLock("Effects");

			onLogin(event.getPlayerCharacter()).on(lock::release);
		}

		@EventHandler
		public void onCombatEntityCreate(CombatEntityCreateEvent event) {
			if (event.getCombatEntity().isPlayer()) {
				Player player = (Player)event.getLivingEntity();
				PlayerCharacter pc = Characters.getPlayerCharacter(player);

				if (dataMap.containsKey(pc.getUniqueCharacterId())) {
					for (PersistingEffect eff : dataMap.get(pc.getUniqueCharacterId())) {
						try {
							effectManager.reapplyEffect(eff);
						} catch (IllegalArgumentException ex) {
							Logger lg = Characters.getInstance().getLogger();
							lg.severe("A skill/spell effect '"
									+ (eff == null ? "null" : eff.getEffectId())
									+ "' was found in the database, but does not exist on this server and could not be reapplied to the player.");
							MessageUtil.sendSevereException(Characters.getInstance(), player, ex);
						}
					}
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerCharacterLogoutEvent(PlayerCharacterLogoutEvent event) {
			PhaseLock lock = event.getLock("Effects");

			onLogout(event.getPlayerCharacter()).on(lock::release);
		}

		@EventHandler
		public void onPlayerCharacterDeleteEvent(PlayerCharacterRemoveEvent event) {
			onDelete(event.getPlayerCharacter());
		}
	}
}
