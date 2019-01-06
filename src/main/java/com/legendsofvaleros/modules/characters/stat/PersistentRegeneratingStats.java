package com.legendsofvaleros.modules.characters.stat;

import com.codingforcookies.doris.query.InsertQuery;
import com.codingforcookies.doris.sql.TableManager;
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
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEntityCreateEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of regenerating stats' levels across logins.
 */
public class PersistentRegeneratingStats {
    private static final RegeneratingStat[] STATS = RegeneratingStat.values();

    private static final String TABLE_NAME = "player_stats";
    private static final String CHARACTER_FIELD = "character_id";
    private static final String STAT_FIELD = "stat_id";
    private static final String VALUE_FIELD = "stat_level";

    private static TableManager managerStats;

    private Map<CharacterId, RegeneratingStatData> dataMap;

    public PersistentRegeneratingStats(DatabaseConfig dbConfig) {
        managerStats = new TableManager(dbConfig.getDbPoolsId(), TABLE_NAME);

        managerStats.primary(CHARACTER_FIELD, "VARCHAR(38)")
                .primary(STAT_FIELD, "VARCHAR(32)")
                .column(VALUE_FIELD, "FLOAT").create();

        dataMap = new ConcurrentHashMap<>();

        Characters.getInstance().registerEvents(new PlayerCharacterListener());
    }

    public void onDisable() {
        // saves any unsaved data. PlayerQuitEvents are not thrown on shutdown, so this is important
        // even if /reload is never used
        for (RegeneratingStatData data : dataMap.values()) {
            Player player = Bukkit.getPlayer(data.id.getPlayerId());
            CombatEntity ce = CombatEngine.getEntity(player);
            updateData(data.id, ce);
            save(data);
        }

        dataMap.clear();
    }

    private void load(CharacterId characterId, PhaseLock lock) {
        if (characterId == null) return;

        managerStats.query()
                .select()
                .where(CHARACTER_FIELD, characterId.toString())
                .build()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    RegeneratingStatData data = null;

                    while (result.next()) {
                        String statName = result.getString(STAT_FIELD);
                        RegeneratingStat stat;
                        try {
                            stat = RegeneratingStat.valueOf(statName);
                        } catch (IllegalArgumentException | NullPointerException ex) {
                            Characters.getInstance().getLogger().warning("Found a stat '" + statName
                                    + "' but no stat with that name exists on this server.");
                            continue;
                        }
                        double value = result.getDouble(VALUE_FIELD);

                        if (data == null) {
                            data = new RegeneratingStatData(characterId);
                        }
                        data.values.put(stat, value);
                    }

                    if (data != null) {
                        dataMap.put(characterId, data);
                    }

                    lock.release();
                })
                .execute(true);
    }

    private ListenableFuture<Void> save(RegeneratingStatData data) {
        SettableFuture<Void> ret = SettableFuture.create();

        if (data == null) ret.set(null);
        else {
            InsertQuery<ResultSet> insert = managerStats.query()
                    .insert()
                    .onDuplicateUpdate(VALUE_FIELD);
            for (Map.Entry<RegeneratingStat, Double> ent : data.values.entrySet()) {
                insert.values(CHARACTER_FIELD, data.id.toString(),
                        STAT_FIELD, ent.getKey().name(),
                        VALUE_FIELD, ent.getValue());
                insert.addBatch();
            }
            insert.build().onFinished(() -> ret.set(null)).execute(true);
        }

        return ret;
    }

    private void updateData(CharacterId characterId, CombatEntity ce) {
        // stores the players current regenerating stat values in the local data map
        RegeneratingStatData current = dataMap.get(characterId);
        if (current == null) {
            current = new RegeneratingStatData(characterId);
            dataMap.put(characterId, current);
        }
        if (ce != null) {
            for (RegeneratingStat stat : STATS) {
                current.values.put(stat, ce.getStats().getRegeneratingStat(stat));
            }
        }
    }

    /**
     * Listens to player-character initialization and logouts.
     */
    private class PlayerCharacterListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onCombatEntityCreate(final CombatEntityCreateEvent event) {
            if (event.getLivingEntity() != null && event.getLivingEntity().getType() == EntityType.PLAYER) {
                Player player = (Player) event.getLivingEntity();

                if (!Characters.isPlayerCharacterLoaded(player)) return;

                PlayerCharacter current = Characters.getPlayerCharacter(player);

                // applies stat numbers to combat entity
                RegeneratingStatData data = dataMap.get(current.getUniqueCharacterId());
                if (data != null) {
                    for (RegeneratingStat stat : STATS) {
                        Double fromDb = data.values.get(stat);
                        if (fromDb != null) {
                            event.getCombatEntity().getStats().setRegeneratingStat(stat, fromDb);
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onPlayerCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            if (!dataMap.containsKey(event.getPlayerCharacter().getUniqueCharacterId())) {
                // checks the database for any regenerating stat data for the loading character
                load(event.getPlayerCharacter().getUniqueCharacterId(), event.getLock("Stats"));
            }
        }

        @EventHandler
        public void onPlayerCharacterLogout(PlayerCharacterLogoutEvent event) {
            updateData(event.getPlayerCharacter().getUniqueCharacterId(), CombatEngine.getInstance()
                    .getCombatEntity(event.getPlayer()));

            // on server logout, saves all locally stored data for the player's characters
            if (event.isServerLogout()) {

                PlayerCharacters characters = Characters.getInstance().getCharacters(event.getPlayer());
                for (PlayerCharacter pc : characters.getCharacterSet()) {

                    final RegeneratingStatData data = dataMap.remove(pc.getUniqueCharacterId());

                    if (data != null) {
                        PhaseLock lock = event.getLock("Stats");
                        save(data).addListener(lock::release, Characters.getInstance().getScheduler()::async);
                    }
                }
            }
        }
    }

    /**
     * Stores regenerating-stat data for a player.
     */
    private class RegeneratingStatData {
        private CharacterId id;
        private Map<RegeneratingStat, Double> values;

        private RegeneratingStatData(CharacterId id) {
            this.id = id;
            values = new HashMap<>();
        }
    }

}
