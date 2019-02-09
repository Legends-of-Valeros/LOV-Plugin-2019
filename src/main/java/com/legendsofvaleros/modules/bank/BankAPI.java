package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.characters.api.CharacterId;

public class BankAPI {
    @ModuleRPC("banks")
    public interface RPC {
        Promise<Bank> find(CharacterId characterId);
        Promise<Boolean> save(CharacterId characterId, Bank bank);
        Promise<Boolean> delete(CharacterId characterId);
    }

    private final RPC rpc;

    public BankAPI() {
        this.rpc = APIController.create(RPC.class);
    }

    public Bank getBank(CharacterId characterId) {
        try {
            return rpc.find(characterId).get();
        } catch (Throwable throwable) {
            return null;
        }
    }
}