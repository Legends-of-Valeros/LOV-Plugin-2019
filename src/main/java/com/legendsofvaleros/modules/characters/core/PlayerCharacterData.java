package com.legendsofvaleros.modules.characters.core;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.api.PlayerCharacters;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.classes.EntityClass;
import com.legendsofvaleros.modules.classes.skills.Skill;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base data about players' characters.
 */
public class PlayerCharacterData {
    private interface RPC {
        Promise<List<CharacterData>> findPlayerCharacters(Object obj);

        Promise<Object> savePlayerCharacter(CharacterData character);

        Promise<Boolean> deletePlayerCharacter(CharacterId characterId);
    }

    private static RPC rpc;

    private static Map<UUID, PlayerCharacterCollection> dataMap;

    static void onEnable() {
        rpc = APIController.create(RPC.class);

        dataMap = new ConcurrentHashMap<>();
    }

    /*static void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            onLogout(player.getUniqueId());
        }
    }*/

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
     * Asynchronously loads basic player-character data on login.
     * @param playerId The name of the player that logged in.
     * @return An asynchronous future result of loading the player characters.
     */
    static Promise<PlayerCharacters> onLogin(final UUID playerId) {
        Promise<PlayerCharacters> promise = new Promise<>();

        JsonObject jo = new JsonObject();
        jo.add("player", new JsonPrimitive(playerId.toString()));
        rpc.findPlayerCharacters(jo).onSuccess(val -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                List<ReusablePlayerCharacter> characters = new LinkedList<>();

                for (CharacterData datum : val.orElse(ImmutableList.of())) {
                    try {
                        CharacterExperience experience = new CharacterExperience(datum.level, datum.progress);
                        ReusablePlayerCharacter character = new ReusablePlayerCharacter(player, datum.id.getCharacterNumber(), datum.race,
                                datum.clazz, datum.location, experience, new PlayerInventoryData(datum.inventory), datum.skills);
                        characters.add(character);
                    } catch (Exception e) {
                        Characters.getInstance().getLogger().severe("could not load character " + datum.id.getCharacterNumber() + " for player " + player.getName());
                        MessageUtil.sendSevereException(Characters.getInstance(), player, e);
                    }
                }

                PlayerCharacterCollection coll = new PlayerCharacterCollection(player, characters);
                dataMap.put(player.getUniqueId(), coll);

                promise.resolve(coll);
            }
        }).onFailure(promise::reject);

        return promise;
    }

    static Promise onLogout(PlayerCharacter pc) {
        pc.getInventoryData().saveInventory(pc);

        return save(pc);
    }

    static void onLogout(UUID playerId) {
        dataMap.remove(playerId);
    }

    public static Promise<Boolean> remove(UUID playerId, int characterId) {
        return rpc.deletePlayerCharacter(new CharacterId(playerId, characterId));
    }

    public static Promise save(PlayerCharacter pc) {
        return rpc.savePlayerCharacter(new CharacterData(pc));
    }

    /**
     * Stores loaded data about a player-character until it can be used on the main thread.
     */
    private static class CharacterData {
        @SerializedName("_id")
        private CharacterId id;
        private UUID player;
        private int number;

        private EntityRace race;

        @SerializedName("class")
        private EntityClass clazz;

        private int level;
        private long progress;

        private Location location;

        private Gear.Data[] inventory;
        protected List<String> skills;

        private CharacterData(PlayerCharacter character) {
            this.id = character.getUniqueCharacterId();
            this.player = character.getPlayerId();
            this.number = character.getCharacterNumber();

            this.race = character.getPlayerRace();
            this.clazz = character.getPlayerClass();

            this.level = character.getExperience().getLevel();
            this.progress = character.getExperience().getExperienceTowardsNextLevel();

            this.location = character.getLocation();

            this.inventory = character.getInventoryData().getData();

            List<Entry<Skill, Integer>> skills = character.getSkillSet().getSkills();

            this.skills = new ArrayList<>();

            for (Entry<Skill, Integer> skillPair : skills) {
                if (skillPair.getValue() > 1) {
                    this.skills.add(skillPair.getKey().getId() + ":" + skillPair.getValue());
                } else {
                    this.skills.add(skillPair.getKey().getId());
                }
            }
        }
    }
}
