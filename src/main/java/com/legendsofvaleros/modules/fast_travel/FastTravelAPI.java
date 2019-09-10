package com.legendsofvaleros.modules.fast_travel;

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
import java.util.HashMap;
import java.util.Map;

public class FastTravelAPI extends Module {
    public interface RPC {
        Promise<PlayerFastTravels> getPlayerFastTravels(CharacterId characterId);

        Promise<Object> savePlayerFastTravels(PlayerFastTravels travels);

        Promise<Boolean> deletePlayerFastTravels(CharacterId characterId);
    }

    private RPC rpc;

    private static Map<CharacterId, PlayerFastTravels> fastTravels = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerListener());
    }

    public Collection<String> getDiscovered(PlayerCharacter pc) {
        return fastTravels.get(pc.getUniqueCharacterId()).locations;
    }

    public boolean hasDiscovered(PlayerCharacter pc, String npcId) {
        return fastTravels.get(pc.getUniqueCharacterId()).locations.contains(npcId);
    }

    public boolean addDiscovered(PlayerCharacter pc, String npcId) {
        return fastTravels.get(pc.getUniqueCharacterId()).locations.add(npcId);
    }

    private Promise onLogin(PlayerCharacter pc) {
        return rpc.getPlayerFastTravels(pc.getUniqueCharacterId()).onSuccess(val ->
                fastTravels.put(pc.getUniqueCharacterId(), val.orElseGet(() -> new PlayerFastTravels(pc))));
    }

    private Promise onLogout(CharacterId characterId) {
        return rpc.savePlayerFastTravels(fastTravels.remove(characterId));
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerFastTravels(characterId);
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Fast Travel");

            onLogin(event.getPlayerCharacter()).on(lock::release);
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
