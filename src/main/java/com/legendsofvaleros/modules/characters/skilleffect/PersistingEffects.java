package com.legendsofvaleros.modules.characters.skilleffect;

import com.codingforcookies.doris.query.InsertQuery;
import com.codingforcookies.doris.sql.TableManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.config.DatabaseConfig;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.characters.skilleffect.PersistingEffect.PersistingEffectBuilder;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Stores and read data to/form a database for character effects that persist across logins.
 */
public class PersistingEffects {
	private static final long MIN_MILLIS_REMAINING_TO_SAVE = 500;
	
	private static final String TABLE_NAME = "player_skilleffect";
	private static final String CHARACTER_ID_FIELD = "character_id";
	private static final String EFFECT_FIELD = "effect_id";
	private static final String REMAINING_DURATION_FIELD = "remaining_duration_millis";
	private static final String ELAPSED_DURATION_FIELD = "elapsed_duration_millis";
	private static final String LEVEL_FIELD = "effect_level";
	private static final String STRING_META_FIELD = "string_meta";
	private static final String BYTE_META_FIELD = "byte_meta";

	private static TableManager managerEffects;

	private static Set<CharacterId> loadedCharacters;
	private static Multimap<CharacterId, PersistingEffect> dataMap;
	private static SkillEffects effectManager;

	public static void onEnable(DatabaseConfig dbConfig, SkillEffects characterEffects) {
		managerEffects = new TableManager(dbConfig.getDbPoolsId(), TABLE_NAME);

		managerEffects.primary(CHARACTER_ID_FIELD, "VARCHAR(36)")
				.primary(EFFECT_FIELD, "VARCHAR(64)")
				.column(REMAINING_DURATION_FIELD, "BIGINT")
				.column(ELAPSED_DURATION_FIELD, "BIGINT")
				.column(LEVEL_FIELD, "INT")
				.column(STRING_META_FIELD, "VARCHAR(255)")
				.column(BYTE_META_FIELD, "VARBINARY(512)").create();
		
		loadedCharacters = new HashSet<>();
		dataMap = HashMultimap.create();
		effectManager = characterEffects;

		Characters.getInstance().registerEvents(new PlayerCharacterListener());
	}

	public static void onDisable() {
		Set<PersistingEffect> saveAll = new HashSet<>();

		saveAll.addAll(dataMap.values());
		dataMap.clear();

		if(!saveAll.isEmpty())
			writeEffects(saveAll, false);
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

	private static void loadEffects(final CharacterId characterId, final PhaseLock lock) {
		managerEffects.query()
							.select()
								.where(CHARACTER_ID_FIELD, characterId.toString())
							.build()
						.callback((result) -> {
							final List<PersistingEffect> fromDb = new ArrayList<>();
							while (result.next()) {
								String effectId = result.getString(EFFECT_FIELD);
								long remaining = result.getLong(REMAINING_DURATION_FIELD);

								PersistingEffectBuilder builder =
										PersistingEffect.newBuilder(effectId, characterId, remaining);

								builder.setElapsedDurationMillis(result.getLong(ELAPSED_DURATION_FIELD));
								builder.setLevel(result.getInt(LEVEL_FIELD));
								builder.setStringMeta(result.getString(STRING_META_FIELD));
								builder.setByteMeta(result.getBytes(BYTE_META_FIELD));

								fromDb.add(builder.build());
							}

							if(!fromDb.isEmpty()) {
								managerEffects.query()
													.remove()
														.where(CHARACTER_ID_FIELD, characterId.toString())
													.build()
												.execute(true);

								// syncs to the main thread before putting data in the cache
								Characters.getInstance().getScheduler().executeInSpigotCircle(() -> {
									PlayerCharacter pc = Characters.getInstance().getCharacter(characterId);
									Player player = Bukkit.getPlayer(characterId.getPlayerId());
									// makes sure the player is still online and valid
									if (pc == null || player == null || !player.isOnline()) {
										return;
									}

									for (PersistingEffect effect : fromDb) {
										dataMap.put(characterId, effect);
									}

									lock.release();
								});

							} else {
								lock.release();
							}
						})
					.execute(true);
	}

	private static ListenableFuture<Void> writeEffects(Set<PersistingEffect> effects, boolean async) {
		SettableFuture<Void> ret = SettableFuture.create();

		if (effects == null || effects.isEmpty()) {
			ret.set(null);
		}else {
			// makes a final, defensive copy of the effects to save
			final Set<PersistingEffect> effs = new HashSet<>();
			for (PersistingEffect effect : effects) {

				// does not bother writing effects with very short remaining durations
				if (effect.getRemainingDurationMillis() >= MIN_MILLIS_REMAINING_TO_SAVE) {
					effs.add(effect);
				}
			}

			if (effs.isEmpty()) {
				ret.set(null);
			}else{
				InsertQuery<ResultSet> insert = managerEffects.query()
						.insert()
						.onDuplicateUpdate(CHARACTER_ID_FIELD,
								EFFECT_FIELD,
								REMAINING_DURATION_FIELD,
								ELAPSED_DURATION_FIELD,
								LEVEL_FIELD,
								STRING_META_FIELD,
								BYTE_META_FIELD);
				for (PersistingEffect eff : effs) {
					insert.values(CHARACTER_ID_FIELD, eff.getAffected().toString(),
							EFFECT_FIELD, eff.getEffectId(),
							REMAINING_DURATION_FIELD, eff.getRemainingDurationMillis(),
							ELAPSED_DURATION_FIELD, eff.getElapsedMillis(),
							LEVEL_FIELD, eff.getLevel(),
							STRING_META_FIELD, eff.getStringMeta(),
							BYTE_META_FIELD, eff.getByteMeta());
					insert.addBatch();
				}
				insert.build().onFinished(() -> ret.set(null)).execute(async);
			}
		}

		return ret;
	}

	/**
	 * Loads and applies persisting effects on player-character logins.
	 */
	private static class PlayerCharacterListener implements Listener {

		@EventHandler
		public void onPlayerCharacterStartLoading(final PlayerCharacterStartLoadingEvent event) {
			if (loadedCharacters.add(event.getPlayerCharacter().getUniqueCharacterId())) {
				final PhaseLock lock = event.getLock("Effects");

				Characters.getInstance().getScheduler().executeInMyCircle(() -> loadEffects(event.getPlayerCharacter().getUniqueCharacterId(), lock));
			}
		}

		@EventHandler
		public void onCombatEntityCreate(CombatEntityCreateEvent event) {
			if (event.getLivingEntity().getType() == EntityType.PLAYER) {
				Player player = (Player) event.getLivingEntity();

				if(!Characters.isPlayerCharacterLoaded(player)) return;

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
							MessageUtil.sendException(Characters.getInstance(), player, ex, true);
						}
					}

					dataMap.removeAll(pc.getUniqueCharacterId());
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerCharacterLogoutEvent(PlayerCharacterLogoutEvent event) {
			if (event.isServerLogout()) {
				PlayerCharacters characters = Characters.getInstance().getCharacters(event.getPlayer());
				if (characters != null) {

					Set<PersistingEffect> saveThese = new HashSet<>();

					for (PlayerCharacter pc : characters.getCharacterSet()) {
						loadedCharacters.remove(pc.getUniqueCharacterId());

						if (dataMap.containsKey(pc.getUniqueCharacterId())) {
							saveThese.addAll(dataMap.removeAll(pc.getUniqueCharacterId()));
						}
					}

					PhaseLock lock = event.getLock("Effects");
					writeEffects(saveThese, true).addListener(lock::release, Characters.getInstance().getScheduler()::async);
				}
			}
		}
	}

}
