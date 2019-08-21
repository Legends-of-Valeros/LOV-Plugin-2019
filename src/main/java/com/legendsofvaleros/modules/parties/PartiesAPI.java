package com.legendsofvaleros.modules.parties;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class PartiesAPI extends Module {
    public interface RPC {
        Promise<PlayerParty> getPartyByMember(CharacterId characterId);
        Promise<Object> saveParty(PlayerParty party);
    }

    private RPC rpc;

    /**
     * Parties are in this map as long as a single player in the party is online.
     * Once all go offline or leave, it is dumped to the database.
     */
    private Map<CharacterId, PlayerParty> parties = new HashMap<>();
    public PlayerParty getPartyByMember(CharacterId uuid) {
        return parties.get(uuid);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        /*registerEvents(new PlayerListener());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(LegendsOfValeros.getInstance(), () -> {
            for (PlayerParty party : parties.values())
                party.updateUI();
        }, 0L, 20L * 5L);*/
    }

    public void addMember(PlayerParty party, CharacterId identifier) {
        parties.put(identifier, party);

        party.getMembers().add(identifier);

        party.onMemberJoin(identifier);
        party.onMemberEnter(identifier);
    }

    public void removeMember(PlayerParty party, CharacterId uuid) {
        parties.remove(uuid);

        party.getMembers().remove(uuid);

        party.onMemberExit(uuid);
        party.onMemberLeave(uuid);

        if (party.getMembers().size() == 0) {
            party.onDisbanded();

            updateParty(party);
        }
    }

    public Promise updateParty(PlayerParty party) {
        if (party == null)
            return new Promise<>();
        return rpc.saveParty(party);
    }

    private Promise<PlayerParty> onLogin(CharacterId characterId) {
        PlayerParty party = getPartyByMember(characterId);
        if (party != null) {
            return Promise.make(() -> party);
        }

        return rpc.getPartyByMember(characterId);
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerJoin(final PlayerCharacterStartLoadingEvent e) {
            PhaseLock lock = e.getLock("Party");

            onLogin(e.getPlayerCharacter().getUniqueCharacterId()).on(lock::release)
                    .onSuccess(party -> {
                        if(!party.isPresent()) return;
                        party.get().onMemberEnter(e.getPlayerCharacter().getUniqueCharacterId());
                    });
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent e) {
            PlayerParty p = getPartyByMember(e.getPlayerCharacter().getUniqueCharacterId());
            if (p == null)
                return;

            p.onMemberExit(e.getPlayerCharacter().getUniqueCharacterId());

            parties.remove(e.getPlayerCharacter().getUniqueCharacterId());
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent e) throws Throwable {
            onLogin(e.getPlayerCharacter().getUniqueCharacterId()).get();

            PlayerParty p = getPartyByMember(e.getPlayerCharacter().getUniqueCharacterId());
            if (p == null) return;

            removeMember(p, e.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}
