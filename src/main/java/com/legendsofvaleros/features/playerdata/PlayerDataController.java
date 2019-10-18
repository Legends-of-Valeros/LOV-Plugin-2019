package com.legendsofvaleros.features.playerdata;

import com.legendsofvaleros.modules.characters.loading.PlayerLock;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Created by Crystall on 08/23/2019
 */
public class PlayerDataController extends PlayerDataApi {

    private static PlayerDataController instance;

    public static PlayerDataController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
    }


    /**
     * Returns the playerdata for the given player name
     * @param name
     * @return
     */
    public PlayerData getPlayerData(UUID name) {
        return null;
    }

    /**
     * Returns the playerdata for the given player
     * @param player
     * @return
     */
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * saves the given player data to the api
     * @param playerData
     */
    public void savePlayerData(PlayerData playerData) {
        super.saveData(playerData);
    }

    /**
     * Loads the player data into memory from the api on login
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerLock lock = PlayerLock.lockPlayer(event.getPlayer());
        onLogin(event.getPlayer().getUniqueId())
                .onSuccess(val -> playerDataHashMap.put(event.getPlayer().getUniqueId(), val.orElse(null)))
                .onFailure(err -> MessageUtil.sendException(this, err.getMessage()))
                .on(lock::release);
    }

    /**
     * Saves the player data onto the api on logout
     * @param event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData playerData = playerDataHashMap.remove(event.getPlayer().getUniqueId());
        saveData(playerData).onFailure(err -> MessageUtil.sendException(this, err.getMessage()));
    }
}
