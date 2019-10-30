package com.legendsofvaleros.modules.graveyard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;

import java.util.List;

// TODO: Implement zone activation?
public class GraveyardAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Graveyard>> findGraveyards();

        Promise<Object> saveGraveyard(Graveyard yard);

        Promise<Boolean> deleteGraveyard(Graveyard yard);
    }

    private RPC rpc;
    Multimap<String, Graveyard> graveyards = HashMultimap.create();

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        this.rpc = APIController.create(GraveyardAPI.RPC.class);

        this.loadAll().get();
    }

    public Promise<List<Graveyard>> loadAll() {
        return rpc.findGraveyards().onSuccess(val -> {
            graveyards.clear();

            val.orElse(ImmutableList.of()).stream()
                    .filter(yard -> yard.getZone() != null).forEach(yard -> {
                graveyards.put(yard.getZone().getId(), yard);
            });

            getLogger().info("Loaded " + graveyards.size() + " graveyards.");
        });
    }

    public Promise addGraveyard(Graveyard yard) {
        graveyards.put(yard.getZone().getId(), yard);

        // If editing is enabled, generate the hologram right away.
        if (LegendsOfValeros.getMode().allowEditing()) {
            getScheduler().sync(yard::getHologram);
        }

        return rpc.saveGraveyard(yard);
    }

    public void removeGraveyard(Graveyard yard) {
        rpc.deleteGraveyard(yard).onSuccess(() -> {
            graveyards.remove(yard.getZone().getId(), yard);
        });
    }
}
