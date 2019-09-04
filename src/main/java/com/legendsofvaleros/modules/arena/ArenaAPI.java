package com.legendsofvaleros.modules.arena;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.queue.QueueController;

import java.util.UUID;

/**
 * Created by Crystall on 08/21/2019
 */
@DependsOn(QueueController.class)
public class ArenaAPI extends ListenerModule {

    public interface RPC {
//        Promise loadPlayerData(UUID uuid);
//
//        Promise savePlayerData(UUID uuid);
//
//        Promise deletePlayerData(UUID uuid);
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
        return null;
//        return rpc.loadPlayerData(uuid);
    }

    public Promise onLogout(UUID uuid) {
        return null;
//        return rpc.savePlayerData(uuid);
    }

    public Promise onCharacterDelete(UUID uuid) {
        return null;
//        return rpc.deletePlayerData(uuid);
    }

}
