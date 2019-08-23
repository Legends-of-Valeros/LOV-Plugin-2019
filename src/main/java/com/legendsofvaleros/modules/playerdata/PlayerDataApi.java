package com.legendsofvaleros.modules.playerdata;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Crystall on 08/23/2019
 */
public class PlayerDataApi extends ListenerModule {

    private interface RPC {
        Promise<PlayerData> getPlayer(UUID uuid);

        Promise<Boolean> savePlayer(PlayerData data);
    }

    private static RPC rpc;

    protected Map<UUID, PlayerData> playerDataHashMap = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
        rpc = APIController.create(RPC.class);
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
    }

    protected Promise<PlayerData> onLogin(UUID uuid) {
        return getData(uuid);
    }

    protected Promise<Boolean> onLogout(PlayerData playerData) {
        return saveData(playerData);
    }


    protected Promise<PlayerData> getData(UUID uuid) {
        return rpc.getPlayer(uuid).onSuccess(player -> {
            if (player.isPresent()) {
                String currentName = Bukkit.getOfflinePlayer(uuid).getName();
                if (currentName != null && ! currentName.equals(player.get().username)) {
                    player.get().username = currentName;
                }
            }
        });
    }

    protected Promise<Boolean> saveData(PlayerData data) {
        return rpc.savePlayer(data);
    }


}
