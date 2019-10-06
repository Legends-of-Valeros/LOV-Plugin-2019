package com.legendsofvaleros.modules.npcs.trait.bank.trade;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.bank.gui.ItemMorphGUI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.util.model.Models;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TraitTrader extends LOVTrait {
    private static final ItemStack SELL_BUTTON = Models.stack("menu-price-button").setName("Sell Items").create();

    private class Entry {
        IGear item;
        int cost;
    }

    private static class BuyGUI extends GUI {
        BuyGUI(Player p, Entry[] items) {
            super("Trader");

            int rows = (int) Math.ceil((items.length + 1) / 9D);
            type(rows);

            slot(8, rows - 1, SELL_BUTTON, (gui, _p, event) -> new SellGUI().open(_p));

            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) continue;

                Gear.Instance instance = items[i].item.newInstance();
                ItemStack item = instance.toStack();

                ItemMeta meta = item.getItemMeta();
                List<String> lore = (meta.getLore() == null ? new ArrayList<>() : meta.getLore());
                lore.add("");
                lore.add(Money.Format.format(items[i].cost));
                lore.add(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left Click " + ChatColor.DARK_GRAY + "to buy]");
                meta.setLore(lore);

                item.setItemMeta(meta);

                final int slotItem = i;
                slot(i, item, (gui, p1, event) -> {
                    if (Money.sub(Characters.getPlayerCharacter(p), items[slotItem].cost)) {
                        p.playSound(p.getLocation(), "ui.transaction", 1F, 1F);
                        ItemUtil.giveItem(Characters.getPlayerCharacter(p), instance);
                    }
                });
            }
        }
    }

    private static class SellGUI extends ItemMorphGUI {
        SellGUI() {
            super("Trader - Sell");
        }

        @Override
        public boolean isBuy() {
            return false;
        }

        @Override
        public boolean isValid(Gear.Instance item) {
            return item.gear.getType().isTradable();
        }

        @Override
        public long getWorth(Gear.Instance item) {
            return Math.round(item.getValue() * item.amount);
        }

        @Override
        public void executeMorph(PlayerCharacter pc, Gear.Instance item) {

        }

        @Override
        public void onCompleted(PlayerCharacter pc) {

        }
    }

    private Entry[] items = new Entry[0];

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.GOLD_INGOT).setName("Trade").create(), (gui, p, event) -> {
            gui.close(p);

            new BuyGUI(p, items).open(p);
        }));
    }
}