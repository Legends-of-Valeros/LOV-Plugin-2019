package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class BankAPI {
    @ModuleRPC("banks")
    public interface RPC {
        Promise<Bank> get(CharacterId characterId);
        Promise<Boolean> save(Bank bank);
        Promise<Boolean> delete(CharacterId characterId);
    }

    private final RPC rpc;

    private static final Map<CharacterId, Bank> banks = new HashMap<>();

    public BankAPI() {
        this.rpc = APIController.create(BankController.getInstance(), RPC.class);

        BankController.getInstance().registerEvents(new PlayerCharacterListener());
    }

    public Bank getBank(CharacterId characterId) {
        return banks.get(characterId);
    }

    private Promise<Boolean> removeBank(CharacterId characterId) {
        return rpc.delete(characterId);
    }

    private Promise<Bank> onLogin(CharacterId characterId) {
        Promise<Bank> promise = rpc.get(characterId);

        promise.onSuccess((bank) -> {
            if(bank == null)
                bank = new Bank(characterId);
            banks.put(characterId, bank);
        });

        return promise;
    }

    private Promise<Boolean> onLogout(CharacterId characterId) {
        Promise<Boolean> promise = new Promise<>();

        Bank bank = banks.remove(characterId);
        if (bank == null)
            promise.resolve(false);
        else
            rpc.save(bank).on((err, val) -> {
                if(err != null) promise.reject(err);
                else promise.resolve(val);
            });

        return promise;
    }

    private class PlayerCharacterListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Bank");

            onLogin(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err))
                    .on(lock::release);
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Bank");

            onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err))
                    .on(lock::release);
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            removeBank(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err));
        }
    }
}