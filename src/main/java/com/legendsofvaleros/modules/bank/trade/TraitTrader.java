package com.legendsofvaleros.modules.bank.trade;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.bank.gui.ItemMorphGUI;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.util.item.Model;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TraitTrader extends LOVTrait {
    private static final ItemStack SELL_BUTTON = Model.stack("menu-price-button").setName("Sell Items").create();

    private static class BuyGUI extends GUI {
        BuyGUI(Player p, GearItem[] gears, int[] costs) {
            super("Trader");

            int rows = (int) Math.ceil((gears.length + 1) / 9D);
            type(rows);

            slot(8, rows - 1, SELL_BUTTON, (gui, _p, event) -> new SellGUI().open(_p));

            for (int i = 0; i < gears.length; i++) {
                if (gears[i] == null) continue;

                GearItem.Instance instance = gears[i].newInstance();
                ItemStack item = instance.toStack();

                ItemMeta meta = item.getItemMeta();
                List<String> lore = (meta.getLore() == null ? new ArrayList<>() : meta.getLore());
                lore.add("");
                lore.add(Money.Format.format(costs[i]));
                lore.add(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left Click " + ChatColor.DARK_GRAY + "to buy]");
                meta.setLore(lore);

                item.setItemMeta(meta);

                final int slotItem = i;
                slot(i, item, (gui, p1, event) -> {
                    if (Money.sub(Characters.getPlayerCharacter(p), costs[slotItem])) {
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
        public boolean isValid(GearItem.Instance item) {
            return item.gear.getType().isTradable();
        }

        @Override
        public long getWorth(GearItem.Instance item) {
            return Math.round(item.getValue() * item.amount);
        }

        @Override
        public void executeMorph(PlayerCharacter pc, GearItem.Instance item) {

        }

        @Override
        public void onCompleted(PlayerCharacter pc) {

        }
    }

    private String[] items = new String[0];
    private int[] costs = new int[0];

    private transient GearItem[] gears = new GearItem[0];

    @Override
    public void onSpawn() {
        gears = new GearItem[items.length];
        for (int i = 0; i < items.length; i++)
            gears[i] = GearItem.fromID(items[i]);
    }

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.GOLD_INGOT).setName("Trade").create(), (gui, p, event) -> {
            gui.close(p);

            new BuyGUI(p, gears, costs).open(p);
        }));
    }
}