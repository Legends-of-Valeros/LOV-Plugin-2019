package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ModuleListener;
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

public class BankAPI extends ModuleListener {
    public interface RPC {
        Promise<Bank> getPlayerBank(CharacterId characterId);

        Promise<Boolean> savePlayerBank(Bank bank);

        Promise<Boolean> deletePlayerBank(CharacterId characterId);
    }

    private RPC rpc;

    private final Map<CharacterId, Bank> banks = new HashMap<>();

    public Bank getBank(CharacterId characterId) {
        return banks.get(characterId);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.rpc = APIController.create(RPC.class);

        registerEvents(new PlayerCharacterListener());
    }

    private Promise<Boolean> removeBank(CharacterId characterId) {
        return rpc.deletePlayerBank(characterId);
    }

    private Promise<Bank> onLogin(CharacterId characterId) {
        Promise<Bank> promise = rpc.getPlayerBank(characterId);

        promise.onSuccess(val -> {
            banks.put(characterId, val.orElseGet(() -> new Bank(characterId)));
        });

        return promise;
    }

    private Promise<Boolean> onLogout(CharacterId characterId) {
        Promise<Boolean> promise = new Promise<>();
        Bank bank = banks.remove(characterId);

        if (bank == null) {
            promise.resolve(false);
        } else {
            rpc.savePlayerBank(bank).on((err, val) -> {
                if (err.isPresent()) promise.reject(err.get());
                else promise.resolve(val.orElse(false));
            });
        }

        return promise;
    }

    public Promise<Boolean> onDelete(CharacterId characterId) {
        return removeBank(characterId);
    }

    private class PlayerCharacterListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Bank");

            onLogin(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((err) -> {
                        MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err);
                        event.getPlayer().kickPlayer("Failed loading PlayerBank - If this error persists, try contacting the support");
                    })
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
            onDelete(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((err) -> MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err));
        }
    }
}