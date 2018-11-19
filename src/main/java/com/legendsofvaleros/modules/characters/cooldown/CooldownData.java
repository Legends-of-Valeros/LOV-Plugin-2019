package com.legendsofvaleros.modules.characters.cooldown;

import com.codingforcookies.doris.query.InsertQuery;
import com.codingforcookies.doris.query.RemoveQuery;
import com.codingforcookies.doris.sql.QueryMethod;
import com.codingforcookies.doris.sql.TableManager;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.Cooldowns;
import com.legendsofvaleros.modules.characters.api.Cooldowns.Cooldown;
import com.legendsofvaleros.modules.characters.api.Cooldowns.CooldownType;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.config.DatabaseConfig;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Stores and loads cooldown data in/from a database to make them persist across logins.
 */
public class CooldownData {
	private static final long MIN_MILLIS_REMAINING_TO_SAVE = 2000;
	private static final long MAX_CALENDAR_AGE = 20160000;

	private static final String TABLE_NAME = "player_cooldowns";
	private static final String ID_FIELD = "character_id";
	private static final String KEY_FIELD = "cooldown_key";
	private static final String TYPE_FIELD = "cooldown_type";
	private static final String TIME_FIELD = "cooldown_time_millis";

	private static TableManager managerCooldowns;

	private static Set<CharacterId> loadedCharacters;

	public static void onEnable(DatabaseConfig dbConfig) {
		managerCooldowns = new TableManager(dbConfig.getDbPoolsId(), TABLE_NAME);

		managerCooldowns.primary(ID_FIELD, "VARCHAR(38)")
				.primary(KEY_FIELD, "VARCHAR(128)")
				.column(TYPE_FIELD, "VARCHAR(32)")
				.column(TIME_FIELD, "BIGINT").create();
		
		managerCooldowns.query()
							.remove()
								.where(TYPE_FIELD, CooldownType.CALENDAR_TIME.name(),
										TIME_FIELD, QueryMethod.LESS_THAN, System.currentTimeMillis() - MAX_CALENDAR_AGE)
							.build()
						.execute(true);

		loadedCharacters = Collections.newSetFromMap(new ConcurrentHashMap<>());

		Characters.getInstance().registerEvents(new PlayerCharacterListener());
	}

	/**
	 * Makes an asynchronous attempt to persistently save cooldown changes to the database record for
	 * a player-character.
	 * 
	 * @param characterId The name of the player-character the cooldowns are for.
	 * @param insertOrUpdate A set of the cooldowns that have changed or been created since they were
	 *        initially loaded from the database.
	 * @param expiredKeys A set of the keys that previously had cooldowns but whose cooldowns have
	 *        since expired.
	 */
	public static void saveCooldowns(final CharacterId characterId,
			Collection<? extends Cooldown> insertOrUpdate, Collection<String> expiredKeys) {
		final Map<Cooldown, Long> update = new HashMap<>();

		for(Cooldown cd : insertOrUpdate) {
			if(cd == null) continue;
			
			long time = 0;
			long remaining = cd.getRemainingDurationMillis();

			if(remaining >= MIN_MILLIS_REMAINING_TO_SAVE) {
				switch (cd.getCooldownType()) {
					case CALENDAR_TIME:
						// a hard calendar-time timestamp of a point in the future
						time = remaining + System.currentTimeMillis();
						break;
					case PLAYER_PLAY_TIME:
					case CHARACTER_PLAY_TIME:
						// a remaining number of milliseconds that need to be ticked down while playing
						time = remaining;
						break;
				}
				update.put(cd, time);
			}
		}
		
		if(!update.isEmpty()) {
			InsertQuery<ResultSet> insert = managerCooldowns.query()
								.insert()
									.onDuplicateUpdate(TYPE_FIELD, TIME_FIELD);
			for(Map.Entry<Cooldown, Long> ent : update.entrySet()) {
				Cooldown cd = ent.getKey();
				long time = ent.getValue();
				if(time <= 0)
					continue;

				insert.values(ID_FIELD, characterId.toString(),
							KEY_FIELD, cd.getKey(),
							TYPE_FIELD, cd.getCooldownType().name(),
							TIME_FIELD, time);
				insert.addBatch();
			}
			insert.build().execute(true);
		}

		if(!expiredKeys.isEmpty()) {
			RemoveQuery<ResultSet> remove = managerCooldowns.query().remove();
			for(String expired : expiredKeys) {
				remove.where(ID_FIELD, characterId.toString(),
								KEY_FIELD, expired);
			}
			remove.build().execute(true);
		}
	}

	private static void load(final CharacterId characterId, final PhaseLock lock) {
		if(characterId == null) return;

		managerCooldowns.query()
							.select()
								.where(ID_FIELD, characterId.toString())
							.build()
						.callback((result) -> {
							Logger lg = Characters.getInstance().getLogger();
							final List<CooldownDatum> fromDb = new ArrayList<>();
							
							while (result.next()) {
								CooldownDatum datum = new CooldownDatum();
								datum.key = result.getString(KEY_FIELD);
								String typeName = result.getString(TYPE_FIELD);
								try {
									datum.type = CooldownType.valueOf(typeName);
								} catch (NullPointerException | IllegalArgumentException ex) {
									lg.severe("Found a cooldown-type '" + typeName
											+ "' but cooldown-type with that name exists on this server.");
									continue;
								}
								datum.time = result.getLong(TIME_FIELD);

								fromDb.add(datum);
							}

							if (!fromDb.isEmpty()) {
								// syncs to the main thread before
								Characters.getInstance().getScheduler().executeInSpigotCircle(() -> {
									PlayerCharacter pc = Characters.getInstance().getCharacter(characterId);
									Player player = Bukkit.getPlayer(characterId.getPlayerId());
									if (pc == null || player == null || !player.isOnline()) {
										return;
									}

									Set<String> expired = new HashSet<>();
									Cooldowns cooldowns = pc.getCooldowns();
									for (CooldownDatum datum : fromDb) {
										long duration = 0;
										switch (datum.type) {
										case CALENDAR_TIME:
											// comes from a hard calendar-time timestamp of a point in the future
											duration = datum.time - System.currentTimeMillis();
											break;

										case CHARACTER_PLAY_TIME:
										case PLAYER_PLAY_TIME:
											// a remaining number of milliseconds that need to be ticked down while playing
											duration = datum.time;
											break;
										}

										if (duration > 0) {
											cooldowns.overwriteCooldown(datum.key, datum.type, duration);
										} else {
											expired.add(datum.key);
										}
									}

									if (!expired.isEmpty()) {
										// deletes the entries that were already expired from the database
										saveCooldowns(characterId, new HashSet<>(), expired);
									}

									lock.release();
								});

							} else {
								lock.release();
							}
						})
					.execute(true);
	}

	/**
	 * Listens to player's events to know when to load their data from the database.
	 */
	private static class PlayerCharacterListener implements Listener {

		@EventHandler
		public void onPlayerCharacterStartLoading(final PlayerCharacterStartLoadingEvent event) {
			if (!loadedCharacters.add(event.getPlayerCharacter().getUniqueCharacterId())) {
				final PhaseLock lock = event.getLock();
				Characters.getInstance().getScheduler().executeInMyCircle(() -> load(event.getPlayerCharacter().getUniqueCharacterId(), lock));
			}
		}

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			PlayerCharacters characters = Characters.getInstance().getCharacters(event.getPlayer());
			if (characters != null) {
				for (PlayerCharacter pc : characters.getCharacterSet()) {
					loadedCharacters.remove(pc.getUniqueCharacterId());
				}
			}
		}
	}

	/**
	 * Stores cooldown data until it can be added on the main thread.
	 */
	private static class CooldownDatum {
		private String key;
		private CooldownType type;
		private long time;
	}
}
