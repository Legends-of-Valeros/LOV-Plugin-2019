package com.legendsofvaleros.modules.factions;

import com.google.common.collect.ImmutableList;
import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.InterfaceTypeAdapter;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.factions.api.IFaction;
import com.legendsofvaleros.modules.factions.core.Faction;
import com.legendsofvaleros.modules.factions.core.PlayerFactionReputation;
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

        Promise<PlayerFactionReputation> getPlayerFactionReputation(CharacterId characterId);

        Promise<Object> savePlayerFactionReputation(PlayerFactionReputation factionReputation);

        Promise<Boolean> deletePlayerFactionReputation(CharacterId characterId);
    }

    private RPC rpc;

    private Map<String, Faction> factions = new HashMap<>();
    private Map<CharacterId, PlayerFactionReputation> playerRep = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        InterfaceTypeAdapter.register(IFaction.class,
                obj -> obj.getId(),
                id -> Promise.make(factions.get(id)));

        registerEvents(new PlayerListener());
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        this.loadAll().get();
    }

    public Promise<List<Faction>> loadAll() {
        return rpc.findFactions().onSuccess(val -> {
            factions.clear();

            val.orElse(ImmutableList.of()).stream().forEach(fac ->
                    factions.put(fac.getId(), fac));

            getLogger().info("Loaded " + factions.size() + " factions.");
        });
    }

    public Faction getFaction(String factionId) {
        return factions.get(factionId);
    }

    public Integer getReputation(IFaction faction, PlayerCharacter pc) {
        return playerRep.get(pc.getUniqueCharacterId()).getReputation(faction);
    }

    public Integer editReputation(IFaction faction, PlayerCharacter pc, int amount) {
        if(faction == null)
            return null;

        int oldRep = playerRep.get(faction).getReputation(faction);
        int newRep = playerRep.get(faction).editReputation(faction, amount);
        int change = newRep - oldRep;

        Bukkit.getPluginManager().callEvent(new FactionReputationChangeEvent(pc, faction, newRep, change));

        return newRep;
    }

    private Promise<PlayerFactionReputation> onLogin(PlayerCharacter pc) {
        return rpc.getPlayerFactionReputation(pc.getUniqueCharacterId()).onSuccess(val ->
                playerRep.put(pc.getUniqueCharacterId(), val.orElseGet(() -> new PlayerFactionReputation(pc))));
    }

    private Promise onLogout(CharacterId characterId) {
        return rpc.savePlayerFactionReputation(playerRep.remove(characterId));
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return rpc.deletePlayerFactionReputation(characterId);
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Factions");

            onLogin(event.getPlayerCharacter()).on(lock::release);
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