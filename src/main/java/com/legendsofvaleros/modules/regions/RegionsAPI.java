package com.legendsofvaleros.modules.regions;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.hearthstones.core.HomePoint;

public class RegionsAPI {
    @ModuleRPC("loottable")
    public interface RPC {
        Promise<HomePoint> get(CharacterId characterId);
        Promise<Boolean> save(HomePoint point);
        Promise<Boolean> remove(HomePoint point);
    }

    private final RPC rpc;

    public RegionsAPI() {
        this.rpc = APIController.create(RegionController.getInstance(), RPC.class);
    }
}
