package com.legendsofvaleros.modules.graveyard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

public class GraveyardAPI extends ModuleListener {
    public interface RPC {
        Promise<List<Graveyard>> findGraveyards();

        Promise<Boolean> saveGraveyard(Graveyard yard);

        Promise<Boolean> deleteGraveyard(Graveyard yard);
    }

    private RPC rpc;
    private Multimap<String, Graveyard> graveyards = HashMultimap.create();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

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
                    .filter(yard -> yard.getZone() != null).forEach(yard ->
                    graveyards.put(yard.getZone().channel, yard));

            getLogger().info("Loaded " + graveyards.size() + " graveyards.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Graveyard getNearestGraveyard(Zone zone, Location loc) {
        if (graveyards == null || graveyards.size() == 0
                || zone == null || !graveyards.containsKey(zone.channel))
            return null;

        Collection<Graveyard> yards = graveyards.get(zone.channel);

        Graveyard closest = null;
        double distance = Double.MAX_VALUE;
        for (Graveyard data : yards) {
            if (loc.distance(data.getLocation()) < distance)
                closest = data;
        }

        return closest;
    }

    public Promise<Boolean> addGraveyard(Graveyard yard) {
        graveyards.put(yard.getZone().channel, yard);

        // If editing is enabled, generate the hologram right away.
        if (LegendsOfValeros.getMode().allowEditing())
            getScheduler().sync(yard::getHologram);

        return rpc.saveGraveyard(yard);
    }

    public Promise<Boolean> removeGraveyard(Graveyard yard) {
        graveyards.remove(yard.getZone().channel, yard);

        return rpc.deleteGraveyard(yard);
    }
}
