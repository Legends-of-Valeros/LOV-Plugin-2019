package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.LegendsOfValeros;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.bank.item.WorthComponent;
import com.legendsofvaleros.modules.bank.quest.ActionAddCurrency;
import com.legendsofvaleros.modules.bank.quest.RepairObjective;
import com.legendsofvaleros.modules.bank.repair.RepairItemEvent;
import com.legendsofvaleros.modules.bank.repair.TraitBlacksmith;
import com.legendsofvaleros.modules.bank.trade.TradeManager;
import com.legendsofvaleros.modules.bank.trade.TraitTrader;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import com.legendsofvaleros.modules.gear.GearRegistry;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.action.stf.ActionFactory;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;

public class Bank extends ListenerModule {
    private static Bank instance;
    public static Bank getInstance() { return instance; }

    private static final Map<String, Currency> currencies = new HashMap<>();

    public static void registerCurrency(String id, Currency currency) {
        if (currencies.containsKey(id))
            throw new RuntimeException("A currency with that ID is already registered.");
        currencies.put(id, currency);
    }

    @Override
    public void onLoad() {
        this.instance = this;

        BankManager.onEnable();
        Money.onEnable();
        Utilities.getCommandManager().loadCommandClass(BankCommands.class);
        ObjectiveFactory.registerType("repair", RepairObjective.class);
        ActionFactory.registerType("currency_give", ActionAddCurrency.class);
        new TradeManager();
        NPCs.registerTrait("banker", TraitBanker.class);
        NPCs.registerTrait("trader", TraitTrader.class);
        NPCs.registerTrait("blacksmith", TraitBlacksmith.class);
        GearRegistry.registerComponent("worth", WorthComponent.class);
    }

    @Override public void onUnload() {
        BankManager.onDisable();
    }

    @EventHandler
    public void onFillInventory(PlayerCharacterInventoryFillEvent event) {
        updateInv(event.getPlayerCharacter());
    }

    public static PlayerBank getBank(PlayerCharacter pc) {
        return BankManager.getBank(pc.getUniqueCharacterId());
    }

    protected static void updateInv(PlayerCharacter pc) {
        PlayerBank bank = Bank.getBank(pc);
        ItemBuilder item = new ItemBuilder(Material.GOLD_INGOT).setName(null);

        if (bank != null) {
            for (Map.Entry<String, Currency> entry : currencies.entrySet()) {
                String display = entry.getValue().getDisplay(bank.getCurrency(entry.getKey()));
                if (display == null) continue;
                item.addLore(display);
            }
        }

        if (Bukkit.isPrimaryThread()) {
            pc.getPlayer().getInventory().setItem(17, item.create());
        } else {
            Bukkit.getScheduler().runTask(LegendsOfValeros.getInstance(), () -> pc.getPlayer().getInventory().setItem(17, item.create()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRepairItem(RepairItemEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

}