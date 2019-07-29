package com.codingforcookies.robert.core;

import com.codingforcookies.robert.core.GUI.Flag;
import com.codingforcookies.robert.slot.ISlotAction;
import com.codingforcookies.robert.slot.SlotUsable;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

public class GUIListener implements Listener {
    private GUI gui;

    public GUIListener(GUI gui) {
        this.gui = gui;
    }

    /**
     * Binds the GUI listeners. This should be done just before opening the GUI to the player.
     */
    public void bind() {
        Robert.plugin().getServer().getPluginManager().registerEvents(this, Robert.plugin());
    }

    /**
     * Unbinds the GUI listeners. This should be done just after closing the GUI.
     */
    public void unbind() {
        InventoryDragEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    /**
     * Helper function for determining if the GUI that threw the event is the GUI we're supposed
     * to be monitoring.
     */
    private boolean isGUI(Player p, Inventory inventory) {
        InventoryView view = gui.getView(p);
        if (view == null) {
            return false;
        }

        return view.getTopInventory() == inventory;
    }

    private boolean isGUI(Player p, InventoryView view) {
        return gui.getView(p) == view;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (! isGUI((Player) event.getWhoClicked(), event.getInventory()))
            return;

        // Drag events are evil. Only allow """dragging""" if only one slot exists. Re-fire it as a normal click event.
        // TODO: Make drag events work like vanilla MC.
        if (event.getInventorySlots().size() == 1) {
            event.getView().setCursor(event.getOldCursor());

            InventoryClickEvent ice;
            onInventoryClick(ice = new InventoryClickEvent(event.getView(),
                    InventoryType.SlotType.CONTAINER,
                    event.getInventorySlots().iterator().next(),
                    ClickType.LEFT, InventoryAction.PLACE_ALL));
            event.setCancelled(ice.isCancelled());
        } else
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (! isGUI((Player) event.getWhoClicked(), event.getInventory())) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
            gui.onClickPlayerInventory(gui, (Player) event.getWhoClicked(), event);
            return;
        }

        ISlotAction action = gui.getSlot(event.getSlot());

        if (! (action instanceof SlotUsable)) {
            event.setCancelled(true);
        }

        if (action != null) {
            action.doAction(gui, (Player) event.getWhoClicked(), event);
        }
    }

    @EventHandler
    public void onInventoryClosed(final InventoryCloseEvent event) {
        if (! isGUI((Player) event.getPlayer(), event.getView()))
            return;

        BukkitRunnable run = null;

        if (gui.isFixed() && ! gui.allowClose) {
            run = new BukkitRunnable() {
                @Override
                public void run() {
                    gui.open((Player) event.getPlayer(), Flag.REPLACE);
                }
            };
        } else {
            // Inventory closed using escape
            if (RobertStack.top((Player) event.getPlayer()) == gui) {
                run = new BukkitRunnable() {
                    @Override
                    public void run() {
                        gui.close((Player) event.getPlayer());
                    }
                };
            }
        }

        if (event.getView().getCursor().getType() != Material.AIR) {
            Player p = (Player) event.getView().getPlayer();
            Item i = p.getWorld().dropItem(p.getLocation(), event.getView().getCursor());
            i.setPickupDelay(0);

            event.getView().setCursor(null);
        }

        // Prevents rebinding close event before the GUI <i>actually</i> closes, resulting in an infinite loop.
        if (run != null) {
            run.runTaskLater(Robert.plugin(), 1L);
        }
    }
}