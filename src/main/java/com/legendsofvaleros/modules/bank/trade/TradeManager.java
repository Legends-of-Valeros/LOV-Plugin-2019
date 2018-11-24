package com.legendsofvaleros.modules.bank.trade;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.hotswitch.Hotswitch;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import com.legendsofvaleros.util.item.Model;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class TradeManager implements Listener {
    private HashMap<UUID, UUID> pendingRequests = new HashMap<>();

    public TradeManager() {
        Bank.getInstance().registerEvents(this);
    }

    public void startTrade(Player p1, Player p2) {
        TradeState state = new TradeState(p1, p2);
        state.open();
    }

    @EventHandler
    public void onCharacterMenuOpen(PlayerMenuOpenEvent event) {
        event.addSlot(new ItemBuilder(Material.GOLD_BLOCK).setName("Request Trade").create(), (gui, p, ice) -> {
            gui.close(p);

            if (event.getClicked() != null) {
                boolean sendRequest = true;

                if (pendingRequests.containsKey(p.getUniqueId())) {
                    if (pendingRequests.get(p.getUniqueId()).equals(event.getClicked().getUniqueId())) {
                        sendRequest = false;

                        startTrade(p, event.getClicked());
                    }

                    pendingRequests.remove(p.getUniqueId());
                }

                if (sendRequest) {
                    pendingRequests.put(event.getClicked().getUniqueId(), p.getUniqueId());

                    MessageUtil.sendUpdate(p, "Trade request sent to " + event.getClicked().getName() + ".");
                    MessageUtil.sendUpdate(event.getClicked(), "You received a trade request from " + p.getName() + ".");
                }
            }
        });
    }

    @EventHandler
    public void onCharacterLogout(PlayerCharacterLogoutEvent e) {
        pendingRequests.remove(e.getPlayer().getUniqueId());
    }
}

class TradeState {
    Player p1, p2;
    TradeGUI g1, g2;

    public TradeState(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;

        g1 = new TradeGUI(this, "Trade: " + p2.getName());
        g2 = new TradeGUI(this, "Trade: " + p1.getName());
    }

    public void open() {
        g1.open(p1);
        g2.open(p2);

        updateSlots(true);
    }

    public void updateSlots(boolean change) {
        if (change) {
            g1.accepted = false;
            g2.accepted = false;
        }

        g1.regenButtons();
        g2.regenButtons();

        for (int i = 0; i < 7; i++) {
            int slot = i < 4 ? i : 5 + i;
            if (g1.getInventory().getItem(slot) != null)
                g2.slot(5 + slot, g1.getInventory().getItem(slot), null);
            if (g2.getInventory().getItem(slot) != null)
                g1.slot(5 + slot, g2.getInventory().getItem(slot), null);
        }

        g1.slot(5, 2, Model.stack("menu-accept-button" + (g2.accepted ? "-pressed" : "")).setName(g2.accepted ? "Accepted" : "Not Accepted").create(), null);
        g2.slot(5, 2, Model.stack("menu-accept-button" + (g1.accepted ? "-pressed" : "")).setName(g1.accepted ? "Accepted" : "Not Accepted").create(), null);

        if (g1.accepted && g2.accepted) {
            for (int i = 0; i < 7; i++) {
                int slot = i < 4 ? i : 5 + i;
                if (g1.getInventory().getItem(slot) != null)
                    ItemUtil.giveItem(Characters.getPlayerCharacter(p2), GearItem.Instance.fromStack(g1.getInventory().getItem(slot)));
                if (g2.getInventory().getItem(slot) != null)
                    ItemUtil.giveItem(Characters.getPlayerCharacter(p1), GearItem.Instance.fromStack(g2.getInventory().getItem(slot)));
            }

            g1.getInventory().clear();
            g2.getInventory().clear();

            close();
        }
    }

    public void close() {
        for (int i = 0; i < 7; i++) {
            int slot = i < 4 ? i : 5 + i;
            if (g1.getInventory().getItem(slot) != null)
                ItemUtil.giveItem(Characters.getPlayerCharacter(p1), GearItem.Instance.fromStack(g1.getInventory().getItem(slot)));
            if (g2.getInventory().getItem(slot) != null)
                ItemUtil.giveItem(Characters.getPlayerCharacter(p2), GearItem.Instance.fromStack(g2.getInventory().getItem(slot)));
        }

        g1.close(p1);
        g2.close(p2);
    }
}

class TradeGUI extends GUI {
    private ItemStack stack;

    @Override
    public void onOpen(Player p, InventoryView view) {
        p.getInventory().setItem(17, Model.merge("menu-ui-trade", (stack = p.getInventory().getItem(17))));
    }

    @Override public void onClose(Player p, InventoryView view) {
        p.getInventory().setItem(17, stack);
    }

    TradeState state;

    boolean accepted = false;
    int available = -1;

    public TradeGUI(TradeState state, String title) {
        super(title);

        fixed();

        this.state = state;

        for (int i = 0; i < 3; i++)
            slot(4, i, Model.stack("empty-slot").create(), null);

        regenButtons();
    }

    public void regenButtons() {
        slot(0, 2, Model.stack("menu-decline-button").setName("Cancel").create(), (gui, p, event) -> state.close());

        slot(3, 2, Model.stack("menu-accept-button" + (accepted ? "-pressed" : "")).setName(accepted ? "You've accepted" : "Accept").create(), (gui, p, event) -> {
            accepted = !accepted;
            state.updateSlots(false);
        });
    }

    @Override
    public void onClickPlayerInventory(GUI gui, Player p, InventoryClickEvent event) {
        if (InventoryManager.hasFixedItem(event.getSlot()) || event.getSlot() <= Hotswitch.SWITCHER_SLOT)
            return;

        GearItem.Instance instance = GearItem.Instance.fromStack(event.getClickedInventory().getItem(event.getSlot()));
        if (instance == null) return;
        if (!instance.gear.getType().isTradable())
            return;

        // Get an available slot
        available = -1;
        ItemStack stack = p.getInventory().getItem(event.getSlot());
        for (int i = 0; i < 7; i++) {
            ItemStack check = gui.getInventory().getItem(i < 4 ? i : 5 + i);
            if (check == null || (check.isSimilar(stack) && check.getAmount() != check.getMaxStackSize())) {
                available = i < 4 ? i : 5 + i;
                break;
            }
        }

        // If one is available
        if (available != -1) {
            // Clone the item, and reduce it to one
            ItemStack newStack = stack.clone();
            newStack.setAmount(1);

            // If the chosen slot is not empty
            if (gui.getSlot(available) != null)
                gui.getInventory().getItem(available).setAmount(gui.getInventory().getItem(available).getAmount() + 1);

                // If a chosen slot is empty
            else {
                final int theSlot = available;
                // Create a new slot action
                gui.slot(theSlot, newStack, (gui1, p1, event1) -> {
                    // Add the item back to the player's inventory
                    ItemUtil.giveItem(Characters.getPlayerCharacter(p1), GearItem.Instance.fromStack(gui1.getInventory().getItem(theSlot)));

                    // Clear the slot
                    gui1.slot(theSlot, (ItemStack) null, null);

                    // Update slots on both sides
                    state.updateSlots(true);
                });

                // Update slots on both sides
                state.updateSlots(true);
            }

            // If the amount is one, remove it from the player's inventory
            if (stack.getAmount() == 1)
                p.getInventory().setItem(event.getSlot(), null);

                // If the stack is not zero, just decrease it by one
            else
                stack.setAmount(stack.getAmount() - 1);
        }
    }

    public void onClose(Player p) {

    }
}