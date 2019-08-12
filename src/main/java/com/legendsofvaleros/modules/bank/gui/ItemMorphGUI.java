package com.legendsofvaleros.modules.bank.gui;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.ISlotAction;
import com.codingforcookies.robert.slot.SlotUsable;
import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.model.Model;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public abstract class ItemMorphGUI extends GUI {
    private static final ItemBuilder ACCEPT_BUTTON = Model.stack("menu-accept-button").setName(null);

    @Override
    public void onClose(Player p, InventoryView view) {
        if (!canInteract) return;

        returnItems(Characters.getPlayerCharacter(p), view);
    }

    protected long cost = 0;

    private boolean canInteract = true;
    private ISlotAction acceptAction;

    protected Gear.Instance[] items = new Gear.Instance[6];

    public ItemMorphGUI(String title) {
        super(title);

        acceptAction = (gui, p, event) -> {
            canInteract = false;

            PlayerCharacter pc = Characters.getPlayerCharacter(p);

            if (isBuy() && Money.get(pc) < cost) {
                MessageUtil.sendError(p, "You don't have enough crowns to do that!");

                if (gui.getView(p) == null)
                    returnItems(pc, event.getView());
            } else {
                for (int y = 0; y < 2; y++)
                    for (int x = 0; x < 3; x++)
                        gui.getInventory().setItem(x + y * 3, null);

                for (int i = 0; i < items.length; i++) {
                    if (items[i] == null) continue;

                    executeMorph(pc, items[i]);

                    items[i] = null;
                }

                if (isBuy())
                    Money.sub(pc, cost);
                else
                    Money.add(pc, cost);

                onCompleted(pc);

                cost = 0;
            }

            updateCost();

            canInteract = true;
        };

        type(InventoryType.DISPENSER);

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++)
                slot(x, y, Material.AIR, new SlotUsable() {
                    @Override
                    public void onPickup(GUI gui, Player p, ItemStack stack, InventoryClickEvent event) {
                        if (!canInteract) {
                            event.setCancelled(true);
                            return;
                        }

                        Gear.Instance instance = Gear.Instance.fromStack(stack);
                        items[event.getSlot()] = null;
                        if (instance == null || !isValid(instance)) {
                            event.setCancelled(true);
                            return;
                        }
                        cost -= getWorth(instance);

                        updateCost();
                    }

                    @Override
                    public void onPlace(GUI gui, Player p, ItemStack stack, InventoryClickEvent event) {
                        if (!canInteract) {
                            event.setCancelled(true);
                            return;
                        }

                        Gear.Instance instance = Gear.Instance.fromStack(stack);
                        if (instance == null || !isValid(instance)) {
                            event.setCancelled(true);
                            return;
                        }

                        items[event.getSlot()] = instance;
                        cost += getWorth(instance);

                        updateCost();
                    }
                });
        }

        slot(0, 2, Model.EMPTY_SLOT, null);
        slot(2, 2, Model.EMPTY_SLOT, null);

        updateCost();
    }

    private void updateCost() {
        ACCEPT_BUTTON.clearLore();

        boolean hasOne = false;
        for (Gear.Instance item : items)
            if (item != null) {
                hasOne = true;
                break;
            }

        if (hasOne)
            ACCEPT_BUTTON.addLore(Money.Format.format(cost), "");

        ACCEPT_BUTTON.addLore(StringUtil.splitForStackLore(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Left Click" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "to complete."));

        slot(1, 2, ACCEPT_BUTTON.create(), acceptAction);
    }

    private void returnItems(PlayerCharacter pc, InventoryView view) {
        // Clear out the slots just in case
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++)
                slot(x, y, Material.AIR, getSlot(x, y));
        }

        if (view != null) {
            ItemUtil.giveItem(pc, Gear.Instance.fromStack(view.getCursor()));
        }
        for (Gear.Instance instance : items)
            if (instance != null) {
                ItemUtil.giveItem(pc, instance);
            }
    }

    public abstract boolean isBuy();

    public abstract boolean isValid(Gear.Instance item);

    public abstract long getWorth(Gear.Instance item);

    public abstract void executeMorph(PlayerCharacter pc, Gear.Instance item);

    public abstract void onCompleted(PlayerCharacter pc);
}