package com.legendsofvaleros.modules.factions;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
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
import java.util.List;
import java.util.Map;

public class FactionAPI extends ListenerModule {
    public interface RPC {
        Promise<List<Faction>> findFactions();

        Promise<Map<String, Integer>> getPlayerFactionReputation(CharacterId characterId);
        Promise<Boolean> savePlayerFactionReputation(CharacterId characterId, Map<String, Integer> factions);
        Promise<Boolean> deletePlayerFactionReputation(CharacterId characterId);
    }

    private RPC rpc;

    private Map<String, Faction> factions = new HashMap<>();
    private Table<CharacterId, String, Integer> playerRep = HashBasedTable.create();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerListener());
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

    public Promise<List<Faction>> loadAll() {
        return rpc.findFactions().onSuccess(val -> {
            factions.clear();

            val.orElse(ImmutableList.of()).stream().forEach(fac ->
                    factions.put(fac.getId(), fac));

            getLogger().info("Loaded " + factions.size() + " factions.");
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
        return rpc.getPlayerFactionReputation(characterId).onSuccess(val -> {
            val.orElse(ImmutableMap.of()).entrySet().stream().forEach((entry) ->
                    playerRep.put(characterId, entry.getKey(), entry.getValue()));
        });
    }

    private Promise<Boolean> onLogout(CharacterId characterId) {
        Promise<Boolean> promise = rpc.savePlayerFactionReputation(characterId, playerRep.row(characterId));

        promise.on(() -> playerRep.row(characterId).clear());

        return promise;
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerFactionReputation(characterId);
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
            onDelete(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}