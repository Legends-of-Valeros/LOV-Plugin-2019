package com.legendsofvaleros.modules.characters.core;

import com.codingforcookies.doris.sql.TableManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.config.DatabaseConfig;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base data about players' characters.
 */
public class PlayerCharacterData {
    private static final String TABLE_NAME = "player_characters";

    private static final String UUID_FIELD = "player_id";
    private static final String CHAR_NUM_FIELD = "character_number";

    private static final String RACE_FIELD = "character_race";
    private static final String CLASS_FIELD = "character_class";
    private static final String LEVEL_FIELD = "character_level";
    private static final String PROGRESS_FIELD = "character_xp_towards_next_level";

    private static final String WORLD_FIELD = "character_world";
    private static final String X_COORD_FIELD = "character_x";
    private static final String Y_COORD_FIELD = "character_y";
    private static final String Z_COORD_FIELD = "character_z";
    private static final String YAW_FIELD = "character_yaw";
    private static final String PITCH_FIELD = "character_pitch";

    private static final String INVENTORY_FIELD = "character_inventory";
    private static final String SKILLSET_FIELD = "character_skillset";

    private static TableManager managerPlayers;

    private static Map<UUID, PlayerCharacterCollection> dataMap;
    private static Set<UUID> unfulfilledInvalidations;

    static void onEnable(DatabaseConfig dbConfig) {
        managerPlayers = new TableManager(dbConfig.getDbPoolsId(), TABLE_NAME);

        managerPlayers.primary(UUID_FIELD, "VARCHAR(36)")
                .primary(CHAR_NUM_FIELD, "INT")
                .column(RACE_FIELD, "VARCHAR(16)")
                .column(CLASS_FIELD, "VARCHAR(16)")
                .column(LEVEL_FIELD, "INT")
                .column(PROGRESS_FIELD, "BIGINT")
                .column(WORLD_FIELD, "VARCHAR(16)")
                .column(X_COORD_FIELD, "FLOAT")
                .column(Y_COORD_FIELD, "FLOAT")
                .column(Z_COORD_FIELD, "FLOAT")
                .column(YAW_FIELD, "FLOAT")
                .column(PITCH_FIELD, "FLOAT")
                .column(INVENTORY_FIELD, "TEXT")
                .column(SKILLSET_FIELD, "TEXT").create();

        dataMap = new ConcurrentHashMap<>();
        unfulfilledInvalidations = Collections.newSetFromMap(new WeakHashMap<>());
    }

    static void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            onLogout(player.getUniqueId());
        }
    }

    /**
     * Gets the player-character data currently in memory for a given player.
     * <p>
     * Only works for online players whose data has been successfully and fully loaded.
     * @param playerId The name of the player whose character data to get.
     * @return The player's character data, if it is currently fully loaded into memory.
     * <code>null</code> if no data is found for the given player name in local memory. If
     * concurrent edits happen across the network, certain elements of these objects may not
     * reflect these changes until synchronized.
     */
    static PlayerCharacterCollection getPlayerCharacters(UUID playerId) {
        return dataMap.get(playerId);
    }

    /**
     * Asynchronously reads the listener level of a player-character from the database.
     * @param characterId The unique name of the character whose level to get.
     * @return The asynchronous, future result of this computation.
     */
    static ListenableFuture<Integer> getExperienceLevel(final CharacterId characterId) {
        final SettableFuture<Integer> ret = SettableFuture.create();

        managerPlayers.query()
                .select()
                .where(UUID_FIELD, characterId.getPlayerId().toString(),
                        CHAR_NUM_FIELD, characterId.getCharacterNumber())
                .limit(1)
                .build()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    ret.set(result.next() ? result.getInt(LEVEL_FIELD) : 0);
                })
                .execute(true);

        return ret;
    }

    /**
     * Asynchronously reads the amount of listener towards their next level of a player-character
     * from the database.
     * @param characterId The unique name of the character whose listener towards the next level to
     *                    get.
     * @return The asynchronous, future result of this computation.
     */
    static ListenableFuture<Long> getExperienceTowardsNextLevel(final CharacterId characterId) {
        final SettableFuture<Long> ret = SettableFuture.create();

        managerPlayers.query()
                .select()
                .where(UUID_FIELD, characterId.getPlayerId().toString(),
                        CHAR_NUM_FIELD, characterId.getCharacterNumber())
                .limit(1)
                .build()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    ret.set(result.next() ? result.getLong(PROGRESS_FIELD) : 0L);
                })
                .execute(true);

        return ret;
    }

    /**
     * Asynchronously loads basic player-character data on login.
     * @param playerId The name of the player that logged in.
     * @return An asynchronous future result of loading the player characters.
     */
    static ListenableFuture<PlayerCharacters> onLogin(final UUID playerId) {
        final SettableFuture<PlayerCharacters> ret = SettableFuture.create();

        managerPlayers.query()
                .select()
                .where(UUID_FIELD, playerId.toString())
                .build()
                .callback((statement, count) -> {
                    ResultSet result = statement.getResultSet();

                    final List<CharacterData> loaded = new LinkedList<>();
                    while (result.next()) {
                        CharacterData datum = new CharacterData();

                        datum.charNum = result.getInt(CHAR_NUM_FIELD);

                        String raceName = result.getString(RACE_FIELD);
                        try {
                            datum.playerRace = EntityRace.valueOf(raceName);
                        } catch (IllegalArgumentException | NullPointerException ex) {
                            MessageUtil.sendException(Characters.getInstance(), null, "A race name '" + raceName + "' was found in the database, "
                                    + "but no race with that name exists on this server.");
                            continue;
                        }

                        String className = result.getString(CLASS_FIELD);
                        try {
                            datum.playerClass = EntityClass.valueOf(className);
                        } catch (IllegalArgumentException | NullPointerException ex) {
                            MessageUtil.sendException(Characters.getInstance(), null, "A race name '" + raceName + "' was found in the database, "
                                    + "but no race with that name exists on this server.");
                            continue;
                        }

                        datum.level = result.getInt(LEVEL_FIELD);
                        datum.progress = result.getLong(PROGRESS_FIELD);

                        datum.worldName = result.getString(WORLD_FIELD);
                        datum.x = result.getDouble(X_COORD_FIELD);
                        datum.y = result.getDouble(Y_COORD_FIELD);
                        datum.z = result.getDouble(Z_COORD_FIELD);
                        datum.yaw = result.getFloat(YAW_FIELD);
                        datum.pitch = result.getFloat(PITCH_FIELD);
                        datum.inventory = result.getString(INVENTORY_FIELD);
                        datum.skillSet = Arrays.asList(result.getString(SKILLSET_FIELD).split(","));

                        loaded.add(datum);
                    }

                    // syncs to the main thread before using this data in calls to the Bukkit API that are
                    // necessary to validate and construct the PlayerCharacter objects
                    Characters.getInstance().getScheduler().executeInSpigotCircle(() -> {
                        Player player = Bukkit.getPlayer(playerId);
                        if (!unfulfilledInvalidations.remove(playerId) && player != null && player.isOnline()) {
                            List<ReusablePlayerCharacter> characters = new LinkedList<>();

                            for (CharacterData datum : loaded) {
                                try {
                                    CharacterExperience experience =
                                            new CharacterExperience(datum.level, datum.progress);

                                    Location loc = new Location(Bukkit.getWorld(datum.worldName), datum.x, datum.y, datum.z, datum.yaw, datum.pitch);

                                    ReusablePlayerCharacter character =
                                            new ReusablePlayerCharacter(player, datum.charNum, datum.playerRace,
                                                    datum.playerClass, loc, experience, new PlayerInventoryData(datum.inventory), datum.skillSet);

                                    characters.add(character);
                                } catch (Exception e) {
                                    Characters.getInstance().getLogger().severe("could not load character " + datum.charNum + " for player " + player.getName());
                                    MessageUtil.sendSevereException(Characters.getInstance(), player, e);
                                }
                            }

                            PlayerCharacterCollection coll =
                                    new PlayerCharacterCollection(player, characters);
                            dataMap.put(player.getUniqueId(), coll);
                            ret.set(coll);
                        }
                    });
                })
                .execute(true);

        return ret;
    }

    /**
     * Saves basic player-character data when for a player when they log out.
     * @param playerId The name of the player logging out.
     */
    static ListenableFuture<Void> onLogout(UUID playerId) {
        SettableFuture<Void> ret = SettableFuture.create();

        PlayerCharacterCollection data = dataMap.remove(playerId);

        if (data == null) {
            unfulfilledInvalidations.add(playerId);

            ret.set(null);
        } else {
            data.onQuit();

            Set<ReusablePlayerCharacter> changed;
            if (!(changed = data.getChanged()).isEmpty()) {
                for (ReusablePlayerCharacter rpc : changed) {
                    if(rpc.isCurrent()) {
                        rpc.getInventoryData().saveInventory(rpc).addListener(() -> {
                            save(rpc);

                            ret.set(null);
                        }, Characters.getInstance().getScheduler()::sync);
                    }else{
                        save(rpc);

                        ret.set(null);
                    }
                }
            }
        }

        return ret;
    }

    public static void remove(UUID playerId, int characterId) {
        managerPlayers.query()
                .remove()
                .where(UUID_FIELD, playerId.toString(),
                        CHAR_NUM_FIELD, characterId)
                .build()
                .execute(true);
    }

    public static void save(ReusablePlayerCharacter pc) {
        Location loc = pc.getLocation();

        List<Entry<Skill, Integer>> skills = pc.getSkillSet().getCharacterSkills();
        StringBuilder sb = new StringBuilder();
        for (Entry<Skill, Integer> skillPair : skills) {
            sb.append(skillPair.getKey().getId());
            if (skillPair.getValue() > 1) {
                sb.append(":");
                sb.append(skillPair.getValue());
            }
            sb.append(",");
        }
        if (sb.toString().endsWith(","))
            sb.substring(0, sb.length() - 2);

        managerPlayers.query()
                .insert()
                .values(UUID_FIELD, pc.getPlayerId().toString(),
                        CHAR_NUM_FIELD, pc.getCharacterNumber(),
                        RACE_FIELD, pc.getPlayerRace().name(),
                        CLASS_FIELD, pc.getPlayerClass().name(),
                        LEVEL_FIELD, pc.getExperience().getLevel(),
                        PROGRESS_FIELD, pc.getExperience().getExperienceTowardsNextLevel(),

                        WORLD_FIELD, loc.getWorld().getName(),
                        X_COORD_FIELD, loc.getX(),
                        Y_COORD_FIELD, loc.getY(),
                        Z_COORD_FIELD, loc.getZ(),
                        YAW_FIELD, loc.getYaw(),
                        PITCH_FIELD, loc.getPitch(),

                        INVENTORY_FIELD, pc.getInventoryData().getData(),
                        SKILLSET_FIELD, sb.toString())
                .onDuplicateUpdate(LEVEL_FIELD, LEVEL_FIELD, PROGRESS_FIELD, PROGRESS_FIELD, WORLD_FIELD,
                        X_COORD_FIELD, Y_COORD_FIELD, Z_COORD_FIELD, YAW_FIELD, PITCH_FIELD,
                        INVENTORY_FIELD, SKILLSET_FIELD)
                .build()
                .execute(false);
    }

    /**
     * Stores loaded data about a player-character until it can be used on the main thread.
     */
    private static class CharacterData {
        private int charNum;
        private EntityRace playerRace;
        private EntityClass playerClass;
        private int level;
        private long progress;
        private String worldName;
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;
        private String inventory;
        protected List<String> skillSet;
    }
}
