package com.legendsofvaleros.modules.guilds;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;

import java.util.ArrayList;

/**
 * Created by Crystall on 09/19/2019
 */
public class GuildAPI extends ListenerModule {
    public interface RPC {
        Promise<Guild> loadGuild(CharacterId characterId);
    }

    private GuildAPI.RPC rpc;
    public ArrayList<Guild> guilds = new ArrayList<>();

    @Override
    public void onLoad() {
        super.onLoad();
        this.rpc = APIController.create(GuildAPI.RPC.class);
    }

    public Promise<Guild> loadGuild(CharacterId characterId) {
        return rpc.loadGuild(characterId);
    }

}
