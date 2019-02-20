package com.legendsofvaleros.modules.fast_travel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;

public class FastTravelAPI extends Module {
    public interface RPC {
        Promise<List<String>> getPlayerFastTravels(CharacterId characterId);

        Promise<Boolean> savePlayerFastTravels(CharacterId characterId, Collection<String> discovered);

        Promise<Boolean> deletePlayerFastTravels(CharacterId characterId);
    }

    private RPC rpc;

    private static Multimap<CharacterId, String> fastTravels = HashMultimap.create();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerListener());
    }

    public Collection<String> getDiscovered(PlayerCharacter pc) {
        return fastTravels.get(pc.getUniqueCharacterId());
    }

    public boolean hasDiscovered(PlayerCharacter pc, String npcId) {
        return fastTravels.get(pc.getUniqueCharacterId()).contains(npcId);
    }

    public boolean addDiscovered(PlayerCharacter pc, String npcId) {
        return fastTravels.put(pc.getUniqueCharacterId(), npcId);
    }

    private Promise<List<String>> onLogin(CharacterId characterId) {
        return rpc.getPlayerFastTravels(characterId).onSuccess(val ->
                val.orElse(ImmutableList.of()).stream().forEach(npcId ->
                    fastTravels.put(characterId, npcId)));
    }

    private Promise<Boolean> onLogout(CharacterId characterId) {
        Promise<Boolean> promise = rpc.savePlayerFastTravels(characterId, fastTravels.get(characterId));

        promise.on(() -> fastTravels.removeAll(characterId));

        return promise;
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerFastTravels(characterId);
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Fast Travel");

            onLogin(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Fast Travel");

            onLogout(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent event) {
            onDelete(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}
