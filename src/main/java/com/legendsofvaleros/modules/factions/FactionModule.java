package com.legendsofvaleros.modules.factions;

import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.factions.event.FactionReputationChangeEvent;
import com.legendsofvaleros.modules.factions.listener.ReputationListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@DependsOn(Characters.class)
public class FactionModule extends ModuleListener {
    private static FactionModule instance;
    public static FactionModule getInstance() { return instance; }

    private static ORMTable<Faction> factionTable;
    private static ORMTable<Reputation> reputationTable;

    private static Map<String, Faction> factions = new HashMap<>();
    private static Table<CharacterId, String, Reputation> playerRep = HashBasedTable.create();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        registerEvents(new ReputationListener());

        String dbPoolId = LegendsOfValeros.getInstance().getConfig().getString("dbpools-database");
        factionTable = ORMTable.bind(dbPoolId, Faction.class);
        reputationTable = ORMTable.bind(dbPoolId, Reputation.class);

        FactionModule.getInstance().registerEvents(new PlayerListener());

		/*NOTIFICATION_UP = AdvancementAPI.builder(new NamespacedKey(this, "factions/up"))
				                .title("Faction Rep+")
				                .description("Faction rep increased.")
				                .icon("minecraft:paper")
				                .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
				                .hidden(true)
				                .toast(true)
				                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
				                .frame(FrameType.TASK)
				            .build();

		NOTIFICATION_DOWN = AdvancementAPI.builder(new NamespacedKey(this, "factions/down"))
				                .title("Faction Rep-")
				                .description("Faction rep decreased.")
				                .icon("minecraft:paper")
				                .trigger(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "impossible"))
				                .hidden(true)
				                .toast(true)
				                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
				                .frame(FrameType.TASK)
				            .build();*/
    }

    public ListenableFuture<Faction> getFaction(String faction_id) {
        final SettableFuture<Faction> ret = SettableFuture.create();

        if (factions.containsKey(faction_id)) {
            ret.set(factions.get(faction_id));
        } else {
            factionTable.query().get(faction_id).forEach((faction, i) -> {
                factions.put(faction.getId(), faction);
                ret.set(faction);
            }).onEmpty(() -> ret.set(null)).execute(true);
        }

        return ret;
    }

    public ListenableFuture<Reputation> getFactionRep(String faction_id, PlayerCharacter pc) {
        final SettableFuture<Reputation> ret = SettableFuture.create();

        if (playerRep.contains(pc.getUniqueCharacterId(), faction_id)) {
            ret.set(playerRep.get(pc.getUniqueCharacterId(), faction_id));
        } else {
            reputationTable.query().get(pc.getUniqueCharacterId().toString(), faction_id).forEach((reputation, i) -> {
                playerRep.put(pc.getUniqueCharacterId(), faction_id, reputation);
                ret.set(reputation);
            }).onEmpty(() -> {
                Reputation reputation = new Reputation(pc.getUniqueCharacterId(), faction_id);
                playerRep.put(pc.getUniqueCharacterId(), faction_id, reputation);
                ret.set(reputation);
            }).execute(true);
        }

        return ret;
    }

    public ListenableFuture<Boolean> editFactionRep(String faction_id, PlayerCharacter pc, int amount) {
        SettableFuture<Boolean> ret = SettableFuture.create();

        ListenableFuture<Reputation> future = getFactionRep(faction_id, pc);

        future.addListener(() -> {
            try {
                Faction faction = getFaction(faction_id).get();
                Reputation rep = future.get();
                if (rep.reputation >= faction.getMaxReputation()) {
                    ret.set(false);
                } else {
                    int change = amount;
                    if (rep.reputation + change > faction.getMaxReputation()) {
                        change = faction.getMaxReputation() - rep.reputation;
                    }
                    rep.reputation += change;
                    playerRep.put(pc.getUniqueCharacterId(), faction_id, rep);

                    Bukkit.getPluginManager().callEvent(new FactionReputationChangeEvent(pc, faction, change, rep.reputation));
                    rep.save(false);
                    ret.set(true);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                ret.set(false);
            }
        }, FactionModule.getInstance().getScheduler()::async);
        return ret;
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerLeave(PlayerCharacterLogoutEvent event) {
            playerRep.row(event.getPlayerCharacter().getUniqueCharacterId()).clear();
        }

        @EventHandler
        public void onPlayerRemoved(PlayerCharacterRemoveEvent event) {
            reputationTable.query().remove(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}