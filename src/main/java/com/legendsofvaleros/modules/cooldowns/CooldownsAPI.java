package com.legendsofvaleros.modules.cooldowns;

import com.google.common.collect.ImmutableMap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.cooldowns.api.Cooldowns;
import com.legendsofvaleros.modules.cooldowns.api.Cooldowns.CooldownType;
import com.legendsofvaleros.modules.cooldowns.cooldown.CharacterCooldowns;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores and loads cooldown data in/from a database to make them persist across logins.
 */
public class CooldownsAPI extends Module {
	private static final long MIN_MILLIS_REMAINING_TO_SAVE = 2000;
	private static final long MAX_CALENDAR_AGE = 20160000;

	private interface RPC {
		Promise<Map<String, CooldownData>> getPlayerCooldowns(CharacterId characterId);
		Promise<Boolean> savePlayerCooldowns(CharacterId characterId, Map<String, CooldownData> cooldowns);
		Promise<Boolean> deletePlayerCooldowns(CharacterId characterId);
	}

	private RPC rpc;

	private Map<CharacterId, CharacterCooldowns> cooldowns = new HashMap<>();
	public Cooldowns getCooldowns(CharacterId id) { return cooldowns.get(id); }

	@Override
	public void onLoad() {
		super.onLoad();

		rpc = APIController.create(RPC.class);

		Characters.getInstance().registerEvents(new PlayerListener());
	}

	private Promise<Map<String, CooldownData>> onLogin(PlayerCharacter pc) {
		return rpc.getPlayerCooldowns(pc.getUniqueCharacterId()).onSuccess(val -> {
			CharacterCooldowns cools = new CharacterCooldowns(pc);

			val.orElse(ImmutableMap.of()).forEach((key, cooldown) -> {
				long duration = 0;
				switch (cooldown.type) {
					case CALENDAR_TIME:
						// comes from a hard calendar-time timestamp of a point in the future
						duration = cooldown.time - System.currentTimeMillis();
						break;

					case CHARACTER_PLAY_TIME:
					//case PLAYER_PLAY_TIME:
						// a remaining number of milliseconds that need to be ticked down while playing
						duration = cooldown.time;
						break;
				}

				if (duration > 0)
					cools.overwriteCooldown(key, cooldown.type, duration);
			});

			cools.onLogin();

			cooldowns.put(pc.getUniqueCharacterId(), cools);
		});
	}

	/**
	 * Makes an asynchronous attempt to persistently save cooldown changes to the database record for
	 * a player-character.
	 */
	private Promise<Boolean> onLogout(PlayerCharacter pc) {
		Map<String, CooldownData> save = new HashMap<>();

		CharacterCooldowns cools = cooldowns.get(pc.getUniqueCharacterId());

		cools.onLogout();

		for(CharacterCooldowns.CharacterCooldown cd : cools.getCooldowns()) {
			if(cd == null) continue;

			cd.unscheduleTask();

			long remaining = cd.getRemainingDurationMillis();

			if(remaining >= MIN_MILLIS_REMAINING_TO_SAVE) {
				CooldownData datum = new CooldownData();

				datum.type = cd.getCooldownType();

				switch (cd.getCooldownType()) {
					case CALENDAR_TIME:
						// a hard calendar-time timestamp of a point in the future
						datum.time = remaining + System.currentTimeMillis();
						break;
					//case PLAYER_PLAY_TIME:
					case CHARACTER_PLAY_TIME:
						// a remaining number of milliseconds that need to be ticked down while playing
						datum.time = remaining;
						break;
				}

				save.put(cd.getKey(), datum);
			}
		}

		return rpc.savePlayerCooldowns(pc.getUniqueCharacterId(), save);
	}

	private Promise<Boolean> onDelete(PlayerCharacter pc) {
		return rpc.deletePlayerCooldowns(pc.getUniqueCharacterId());
	}

	/**
	 * Listens to player's events to know when to load their data from the database.
	 */
	private class PlayerListener implements Listener {
		@EventHandler
		public void onPlayerCharacterStartLoading(final PlayerCharacterStartLoadingEvent event) {
			PhaseLock lock = event.getLock("Cooldowns");

			onLogin(event.getPlayerCharacter()).on(lock::release);
		}

		@EventHandler
		public void onPlayerQuit(PlayerCharacterLogoutEvent event) {
			PhaseLock lock = event.getLock("Cooldowns");

			onLogout(event.getPlayerCharacter()).on(lock::release);
		}

		@EventHandler
		public void onPlayerDelete(PlayerCharacterRemoveEvent event) {
			onDelete(event.getPlayerCharacter());
		}
	}

	/**
	 * Stores cooldown data until it can be added on the main thread.
	 */
	private static class CooldownData {
		private CooldownType type;
		private long time;
	}
}
