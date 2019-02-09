package com.legendsofvaleros.modules.factions;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.factions.core.Faction;
import com.legendsofvaleros.modules.factions.event.FactionReputationChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class FactionAPI {
    @ModuleRPC("factions")
    public interface RPC {
        Promise<Faction[]> find();

        Promise<Map<String, Integer>> getRep(CharacterId characterId);
        Promise<Boolean> updateRep(CharacterId characterId, Map<String, Integer> factions);
        Promise<Boolean> deleteRep(CharacterId characterId);
    }

    private final RPC rpc;

    private static Map<String, Faction> factions = new HashMap<>();
    private static Table<CharacterId, String, Integer> playerRep = HashBasedTable.create();

    public FactionAPI() {
        this.rpc = APIController.create(FactionController.getInstance(), RPC.class);

        FactionController.getInstance().registerEvents(new PlayerListener());
    }

    public Promise<Faction[]> loadAll() {
        return rpc.find().onSuccess((facs) -> {
            factions.clear();

            for(Faction fac : facs)
                factions.put(fac.getId(), fac);

            FactionController.getInstance().getLogger().info("Loaded " + factions.size() + " factions.");
        }).onFailure(Throwable::printStackTrace);
    }

    public Faction getFaction(String factionId) {
        return factions.get(factionId);
    }

    public Integer getRep(String factionId, PlayerCharacter pc) {
        return playerRep.get(pc.getUniqueCharacterId(), factionId);
    }

    public Integer editRep(String factionId, PlayerCharacter pc, int amount) {
        Faction faction = getFaction(factionId);

        int newRep = Math.min(getRep(factionId, pc) + amount, faction.getMaxReputation());
        int change = newRep - faction.getMaxReputation();

        playerRep.put(pc.getUniqueCharacterId(), factionId, newRep);

        Bukkit.getPluginManager().callEvent(new FactionReputationChangeEvent(pc, faction, newRep, change));

        return newRep;
    }

    private Promise<Map<String, Integer>> onLogin(CharacterId characterId) {
        Promise<Map<String, Integer>> promise = rpc.getRep(characterId);

        promise.onSuccess((map) ->
                map.entrySet().stream().forEach((entry) ->
                        playerRep.put(characterId, entry.getKey(), entry.getValue())));

        return promise;
    }

    private Promise<Void> onLogout(CharacterId characterId) {
        Promise<Void> promise = new Promise<>();

        rpc.updateRep(characterId, playerRep.row(characterId)).on(() -> {
            playerRep.row(characterId).clear();
            promise.resolve(null);
        });

        return promise;
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Factions");

            onLogin(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Factions");

            onLogout(event.getPlayerCharacter().getUniqueCharacterId()).on(lock::release);
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent event) {
            rpc.deleteRep(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}