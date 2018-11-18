package com.legendsofvaleros.modules.parties;

import com.codingforcookies.doris.orm.ORMTable;
import com.codingforcookies.doris.orm.annotation.Column;
import com.codingforcookies.doris.orm.annotation.Table;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class PartyManager {
    @Table(name = "player_parties")
    public static class Pair {
        @Column(primary = true, name = "party_id", length = 32)
        private UUID partyId;

        @Column(primary = true, unique = true, name = "character_id")
        private CharacterId characterId;

        public Pair(UUID partyId, CharacterId characterId) {
            this.partyId = partyId;
            this.characterId = characterId;
        }
    }

    private static ORMTable<Pair> partyTable;

    /**
     * Parties are in this map as long as a single player in the party is online.
     * Once all go offline or leave, it is dumped to the database.
     */
    private static Map<CharacterId, IParty> parties = new HashMap<>();

    /**
     * Get the party for the specified UUID.
     * @param uuid The UUID.
     * @return The party that the UUID is in. Returns null if it is not in a party.
     */
    public static IParty getPartyByMember(CharacterId uuid) {
        return parties.get(uuid);
    }

    public static void onEnable(JavaPlugin plugin) {
        partyTable = ORMTable.bind(plugin.getConfig().getString("dbpools-database"), Pair.class);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (IParty party : parties.values())
                party.updateUI();
        }, 0L, 20L * 5L);
    }

    public static void addMember(IParty party, CharacterId identifier) {
        parties.put(identifier, party);

        party.getMembers().add(identifier);

        party.onMemberJoin(identifier);
        party.onMemberEnter(identifier);
    }

    public static void removeMember(IParty party, CharacterId uuid) {
        parties.remove(uuid);

        party.getMembers().remove(uuid);

        party.onMemberExit(uuid);
        party.onMemberLeave(uuid);

        if (party.getMembers().size() == 0) {
            party.onDisbanded();

            updateParty(party);
        }
    }

    public static void updateParty(final IParty party) {
        if (party == null)
            return;

        List<Pair> members = new ArrayList<>();

        for (CharacterId characterId : party.getMembers())
            members.add(new Pair(party.getUniqueId(), characterId));

        partyTable.query()
                .remove(party.getUniqueId().toString())
                .onFinished(() -> {
                    if (members.size() > 0)
                        partyTable.saveAll(members, true);
                })
                .execute(true);
    }

    private static ListenableFuture<PlayerParty> loadPlayer(final CharacterId uuid) {
        SettableFuture<PlayerParty> ret = SettableFuture.create();

        if (uuid == null) {
            ret.set(null);
        } else {
            // Check if the party is still in the cache
            IParty party = getPartyByMember(uuid);
            if (party != null) {
                ret.set((PlayerParty) party);
            } else {
                partyTable.query()
                        .select()
                        .where("character_id", uuid.toString())
                        .build()
                        .onEmpty(() -> ret.set(null))
                        .forEach((pair) -> {
                            PlayerParty p = new PlayerParty(pair.partyId);

                            // Now that we have the party UUID, lets load all players in the party.
                            partyTable.query()
                                    .get(pair.partyId.toString())
                                    .forEach((pp) -> {
                                        parties.put(pp.characterId, p);
                                        p.getMembers().add(pp.characterId);
                                    })
                                    .onFinished(() -> ret.set(p)).execute(true);
                        }).execute(true);
            }
        }

        return ret;
    }

    private static class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerJoin(final PlayerCharacterStartLoadingEvent e) {
            PhaseLock lock = e.getLock();

            ListenableFuture<PlayerParty> future = loadPlayer(e.getPlayerCharacter().getUniqueCharacterId());
            future.addListener(() -> {
                try {
                    PlayerParty p = future.get();
                    if (p != null)
                        p.onMemberEnter(e.getPlayerCharacter().getUniqueCharacterId());
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }

                lock.release();
            }, Utilities.syncExecutor());
        }

        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent e) {
            final IParty p = getPartyByMember(e.getPlayerCharacter().getUniqueCharacterId());
            if (p == null)
                return;

            p.onMemberExit(e.getPlayerCharacter().getUniqueCharacterId());

            parties.remove(e.getPlayerCharacter().getUniqueCharacterId());
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent e) throws ExecutionException, InterruptedException {
            loadPlayer(e.getPlayerCharacter().getUniqueCharacterId()).get();

            IParty p = getPartyByMember(e.getPlayerCharacter().getUniqueCharacterId());
            if (p == null) return;

            removeMember(p, e.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}