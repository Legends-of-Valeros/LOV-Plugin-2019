package com.legendsofvaleros.modules.bank;

import com.codingforcookies.doris.orm.ORMTable;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.modules.gear.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BankManager {
    private static ORMTable<PlayerBank.Currency> bankCurrencyTable;
    private static ORMTable<PlayerBank.Entry> bankContentTable;

    private static final Map<CharacterId, PlayerBank> banks = new HashMap<>();

    public static PlayerBank getBank(CharacterId characterId) {
        return banks.get(characterId);
    }

    public static void onEnable() {
        String dbPoolId = LegendsOfValeros.getInstance().getConfig().getString("dbpools-database");

        bankCurrencyTable = ORMTable.bind(dbPoolId, PlayerBank.Currency.class);
        bankContentTable = ORMTable.bind(dbPoolId, PlayerBank.Entry.class, ItemManager.gson);

        Bank.getInstance().registerEvents(new PlayerCharacterListener());
    }

    public static void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!Characters.isPlayerCharacterLoaded(p)) continue;
            onLogout(Characters.getPlayerCharacter(p).getUniqueCharacterId());
        }
    }

    private static ListenableFuture<Void> loadBank(CharacterId characterId) {
        SettableFuture<Void> ret = SettableFuture.create();

        PlayerBank bank = new PlayerBank(characterId);
        AtomicInteger done = new AtomicInteger(2);
        Runnable finished = () -> {
            if (done.decrementAndGet() == 0) {
                banks.put(characterId, bank);
                ret.set(null);
            }
        };

        bankCurrencyTable.query()
                .get(characterId)
                .forEach((currency) -> bank.setCurrency(currency.getCurrencyId(), currency.amount))
                .onFinished(finished)
                .execute(true);

        bankContentTable.query()
                .get(characterId)
                .forEach((entry) -> bank.content.put(entry.index, entry))
                .onFinished(finished)
                .execute(true);

        return ret;
    }

    private static ListenableFuture<Void> onLogout(final CharacterId characterId) {
        SettableFuture<Void> ret = SettableFuture.create();

        PlayerBank bank = banks.remove(characterId);
        if (bank == null)
            ret.set(null);
        else {
            AtomicInteger done = new AtomicInteger(2);
            Runnable finished = () -> {
                if (done.decrementAndGet() == 0)
                    ret.set(null);
            };

            bankCurrencyTable.saveAll(bank.getCurrencies(), true)
                    .addListener(finished, Bank.getInstance().getScheduler()::async);

            bankContentTable.query()
                    .remove(characterId)
                    .onFinished(() -> {
                        if (bank.content.size() == 0) {
                            finished.run();
                            return;
                        }

                        bankContentTable.saveAll(bank.content.values(), true)
                                .addListener(finished, Bank.getInstance().getScheduler()::async);
                    })
                    .execute(true);
        }

        return ret;
    }

    private static void onRemove(final String character_id) {
        bankCurrencyTable.query().remove(character_id).execute(true);

        bankContentTable.query().remove(character_id).execute(true);
    }

    private static class PlayerCharacterListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Bank");

            loadBank(event.getPlayerCharacter().getUniqueCharacterId())
                    .addListener(lock::release, Bank.getInstance().getScheduler()::async);
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Bank");
            onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                    .addListener(() -> {
                        banks.remove(event.getPlayerCharacter().getUniqueCharacterId());
                        lock.release();
                    }, Bank.getInstance().getScheduler()::async);
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            onRemove(event.getPlayerCharacter().getUniqueCharacterId().toString());
        }
    }
}