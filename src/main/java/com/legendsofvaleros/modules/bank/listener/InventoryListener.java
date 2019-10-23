package com.legendsofvaleros.modules.bank.listener;

import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.bank.core.Currency;
import com.legendsofvaleros.modules.bank.event.PlayerCurrencyChangeEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public class InventoryListener implements Listener {
    @EventHandler
    public void onPlayerFinishedLoading(PlayerCharacterFinishLoadingEvent event) {
        updateInv(event.getPlayerCharacter());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCurrencyChangeEvent(PlayerCurrencyChangeEvent event) {
        if(event.isCancelled()) return;

        BankController.getInstance().getScheduler().executeInSpigotCircle(() -> updateInv(event.getPlayerCharacter()));
    }

    protected static void updateInv(PlayerCharacter pc) {
        Bank bank = BankController.getBank(pc);
        ItemBuilder item = new ItemBuilder(Material.GOLD_INGOT).setName(null);

        if (bank != null) {
            for (Map.Entry<String, Currency> entry : BankController.getInstance().getCurrencies()) {
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
