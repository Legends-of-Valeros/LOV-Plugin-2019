package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.bank.commands.BankCommands;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.bank.core.Currency;
import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.bank.listener.InventoryListener;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.trait.bank.trade.TradeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@DependsOn(Characters.class)
@ModuleInfo(name = "Banks", info = "")
public class BankController extends Module {
    private static BankController instance;
    public static BankController getInstance() { return instance; }

    private BankAPI api;
    public BankAPI getApi() { return api; }

    private static final Map<String, Currency> currencies = new HashMap<>();
    public Currency getCurrency(String id) {
        return currencies.get(id);
    }
    public Set<Map.Entry<String, Currency>> getCurrencies() { return currencies.entrySet(); }
    public static void registerCurrency(String id, Currency currency) {
        if (currencies.containsKey(id))
            throw new RuntimeException("A currency with that ID is already registered.");
        currencies.put(id, currency);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        this.api = new BankAPI();

        registerEvents(new InventoryListener());

        Money.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new BankCommands());

        new TradeManager();
    }

    public static Bank getBank(PlayerCharacter pc) {
        return instance.getApi().getBank(pc.getUniqueCharacterId());
    }
}