package com.legendsofvaleros.modules.bank;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.npcs.trait.bank.trade.TradeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

@DependsOn(Characters.class)
public class BankController extends ModuleListener {
    private static BankController instance;
    public static BankController getInstance() { return instance; }

    private static final Map<String, Currency> currencies = new HashMap<>();

    public Currency getCurrency(String id) {
        return currencies.get(id);
    }

    public static void registerCurrency(String id, Currency currency) {
        if (currencies.containsKey(id))
            throw new RuntimeException("A currency with that ID is already registered.");
        currencies.put(id, currency);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        BankManager.onEnable();
        Money.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new BankCommand());

        new TradeManager();
    }

    @Override
    public void onUnload() {
        super.onUnload();

        BankManager.onDisable();
    }

    @EventHandler
    public void onPlayerFinishedLoading(PlayerCharacterFinishLoadingEvent event) {
        updateInv(event.getPlayerCharacter());
    }

    public static Bank getBank(PlayerCharacter pc) {
        return BankManager.getBank(pc.getUniqueCharacterId());
    }

    protected static void updateInv(PlayerCharacter pc) {
        Bank bank = BankController.getBank(pc);
        ItemBuilder item = new ItemBuilder(Material.GOLD_INGOT).setName(null);

        if (bank != null) {
            for (Map.Entry<String, Currency> entry : currencies.entrySet()) {
                String display = entry.getValue().getDisplay(bank.getCurrency(entry.getKey()));
                if (display == null) continue;
                item.addLore(display);
            }
        }else
            item.addLore(ChatColor.RED + " - BANK DATA EMPTY - ");

        if (Bukkit.isPrimaryThread()) {
            pc.getPlayer().getInventory().setItem(17, item.create());
        } else {
            BankController.getInstance().getScheduler().executeInSpigotCircle(() -> pc.getPlayer().getInventory().setItem(17, item.create()));
        }
    }
}