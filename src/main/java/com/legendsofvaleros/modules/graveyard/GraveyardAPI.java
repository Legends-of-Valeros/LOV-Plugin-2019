package com.legendsofvaleros.modules.graveyard;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraveyardAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Graveyard>> findGraveyards();

        Promise<Boolean> saveGraveyard(Graveyard yard);

        Promise<Boolean> deleteGraveyard(Graveyard yard);
    }

    private RPC rpc;
    HashMap<String, List<Graveyard>> graveyards = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        this.rpc = APIController.create(GraveyardAPI.RPC.class);

        try {
            this.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Promise<List<Graveyard>> loadAll() {
        return rpc.findGraveyards().onSuccess(val -> {
            graveyards.clear();

            val.orElse(ImmutableList.of()).stream()
                    .filter(yard -> yard.getZone() != null).forEach(yard -> {
                if (graveyards.containsKey(yard.getZone().channel)) {
                    graveyards.get(yard.getZone().channel).add(yard);
                    return;
                }
                ArrayList<Graveyard> yards = new ArrayList<>();
                yards.add(yard);
                graveyards.put(yard.getZone().channel, yards);
            });

            getLogger().info("Loaded " + graveyards.size() + " graveyards.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Promise<Boolean> addGraveyard(Graveyard yard) {
        graveyards.get(yard.getZone().channel).add(yard);

        // If editing is enabled, generate the hologram right away.
        if (LegendsOfValeros.getMode().allowEditing()) {
            getScheduler().sync(yard::getHologram);
        }

        return rpc.saveGraveyard(yard);
    }

    public void removeGraveyard(Graveyard yard) {
        rpc.deleteGraveyard(yard).onSuccess(() -> {
            graveyards.remove(yard.getZone().channel, yard);
        }).onFailure(Throwable::printStackTrace);
    }
}
