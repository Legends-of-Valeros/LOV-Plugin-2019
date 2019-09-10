package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterRemoveEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterStartLoadingEvent;
import com.legendsofvaleros.modules.characters.loading.PhaseLock;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class BankAPI extends ListenerModule {
    public interface RPC {
        Promise<Bank> getBank(String id);

        Promise<Object> saveBank(Bank bank);

        Promise<Boolean> deleteBank(String id);
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

    private Promise<Boolean> removeBank(String id) {
        return rpc.deleteBank(id);
    }

    private Promise<Bank> onLogin(CharacterId characterId) {
        Promise<Bank> promise = rpc.getBank(characterId.toString());

        promise.onSuccess(val -> {
            banks.put(characterId, val.orElseGet(() -> new Bank(characterId)));
        });

        return promise;
    }

    private Promise onLogout(CharacterId characterId) {
        Bank bank = banks.remove(characterId);

        if(bank == null)
            return Promise.make(false);

        return rpc.saveBank(bank);
    }

    public Promise onDelete(CharacterId characterId) {
        return removeBank(characterId.toString());
    }

    private class PlayerCharacterListener implements Listener {
        @EventHandler
        public void onCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
            PhaseLock lock = event.getLock("Bank");

            onLogin(event.getPlayerCharacter().getUniqueCharacterId())
                    .onFailure((err) -> {
                        MessageUtil.sendSevereException(BankController.getInstance(), event.getPlayer(), err);
                        getScheduler().executeInSpigotCircle(new InternalTask(() -> {
                            event.getPlayer().kickPlayer("Failed loading PlayerBank - If this error persists, try contacting the support");
                        }));
                    })
                    .on(lock::release);
        }

        @EventHandler
        public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
            PhaseLock lock = event.getLock("Bank");

            onLogout(event.getPlayerCharacter().getUniqueCharacterId())
                    .on(lock::release);
        }

        @EventHandler
        public void onCharacterRemoved(PlayerCharacterRemoveEvent event) {
            onDelete(event.getPlayerCharacter().getUniqueCharacterId());
        }
    }
}