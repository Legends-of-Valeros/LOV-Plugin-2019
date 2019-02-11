package com.legendsofvaleros.modules.mount;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.hearthstones.core.HomePoint;

public class MountAPI {
    public interface RPC {
        Promise<HomePoint> getMounts(CharacterId characterId);
        Promise<Boolean> saveMounts(CharacterId characterId);
        Promise<Boolean> removeMounts(CharacterId characterId);
    }

    private final RPC rpc;

    public MountAPI() {
        this.rpc = APIController.create(MountsController.getInstance(), RPC.class);
    }
}
