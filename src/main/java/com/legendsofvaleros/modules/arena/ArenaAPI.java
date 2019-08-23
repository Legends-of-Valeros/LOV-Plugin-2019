package com.legendsofvaleros.modules.arena;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;

import java.util.UUID;

/**
 * Created by Crystall on 08/21/2019
 */
public class ArenaAPI extends ListenerModule {

    public interface RPC {
        Promise loadPlayerData(UUID uuid);

        Promise savePlayerData(UUID uuid);

        Promise deletePlayerData(UUID uuid);
    }

    private RPC rpc;

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    public Promise onLogin(UUID uuid) {
        return rpc.loadPlayerData(uuid);
    }

    public Promise onLogout(UUID uuid) {
        return rpc.savePlayerData(uuid);
    }

    public Promise onCharacterDelete(UUID uuid) {
        return rpc.deletePlayerData(uuid);
    }

}
