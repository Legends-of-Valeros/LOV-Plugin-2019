package com.codingforcookies.robert.core;

import com.codingforcookies.robert.slot.ISlotAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Stumblinbear
 */
public class GUI {
    public enum Flag {
        /**
         * Clear the parent stack then open the UI.
         */
        NO_PARENTS,

        /**
         * Replace the UI with the current one in the stack.
         */
        REPLACE
    }

    boolean allowClose = false;
    private boolean fixed = false;

    public boolean isFixed() {
        return fixed;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    private InventoryType type = InventoryType.CHEST;
    private int columns = 9;
    private Inventory inv;

    public Inventory getInventory() {
        return inv;
    }

    public HashMap<UUID, InventoryView> views;

    public InventoryView getView(Player p) {
        return views.get(p.getUniqueId());
    }

    private GUIListener listener;

    private ISlotAction[] slots;

    public ISlotAction getSlot(int i) {
        if (i >= slots.length)
            return null;
        if (i < 0)
            return null;
        return slots[i];
    }

    public ISlotAction getSlot(int x, int y) {
        return getSlot(x + y * columns);
    }

    public GUI(String title) {
        this.title = ChatColor.WHITE + title;

        this.inv = Bukkit.createInventory(null, type, this.title);
        this.slots = new ISlotAction[inv.getSize()];
        this.views = new HashMap<>();
        this.listener = new GUIListener(this);
    }

    /**
     * Fixes the GUI to the screen. i.e. The user is unable to close it via conventional means.
     * <br>
     * This is usually <b>NOT</b> to be used. As it can be an annoyance to players.
     */
    public GUI fixed() {
        fixed = true;
        return this;
    }

    /**
     * Specify what type of GUI should be opened.
     * <p>
     * This recreates the inventory. Any set slots are cleared.
     * <p>
     * <b>Default:</b> CHEST
     * @param type The InventoryType
     * @return The GUI instance
     */
    public GUI type(InventoryType type) {
        this.type = type;
        this.columns = (type == InventoryType.DISPENSER || type == InventoryType.DROPPER || type == InventoryType.HOPPER ? 3 : 9);
        this.inv = Bukkit.createInventory(null, type, title);
        this.slots = new ISlotAction[inv.getSize()];
        return this;
    }

    /**
     * Creates a chest GUI with a set number of rows
     * @param rows The number of rows
     * @return The GUI instance
     */
    public GUI type(int rows) {
        this.type = InventoryType.CHEST;
        this.columns = 9;
        this.inv = Bukkit.createInventory(null, rows * columns, title);
        this.slots = new ISlotAction[inv.getSize()];
        return this;
    }

    /**
     * Set a slot in the inventory to a specified Material; amount: 1.
     * @param slot The slot to be filled.
     * @param item The Material being set into the slot.
     * @return The GUI instance
     */
    public GUI slot(int slot, Material item, ISlotAction action) {
        return slot(slot, new ItemStack(item), action);
    }

    /**
     * Set a slot in the inventory to a specified Material at location; amount: 1.
     * @param x    The x coordinate to be filled.
     * @param y    The y coordinate to be filled.
     * @param item The Material being set into the slot.
     * @return The GUI instance
     */
    public GUI slot(int x, int y, Material item, ISlotAction action) {
        return slot(x, y, new ItemStack(item), action);
    }

    /**
     * Set a slot in the inventory to a specified itemstack.
     * @param slot The slot to be filled.
     * @param item The ItemStack being set into the slot.
     * @return The GUI instance
     */
    public GUI slot(int slot, ItemStack item, ISlotAction action) {
        inv.setItem(slot, item);
        slots[slot] = action;
        return this;
    }

    /**
     * Set at location in the inventory to a specified itemstack.
     * @param x    The x coordinate to be filled.
     * @param y    The y coordinate to be filled.
     * @param item The ItemStack being set into the slot.
     * @return The GUI instance
     */
    public GUI slot(int x, int y, ItemStack item, ISlotAction action) {
        return slot(x + y * columns, item, action);
    }

    public GUI setItem(int slot, ItemStack item) {
        inv.setItem(slot, item);
        return this;
    }

    public GUI setItem(int x, int y, ItemStack item) {
        setItem(x + y * columns, item);
        return this;
    }

    /**
     * Opens the GUI to the specified player.
     */
    public void open(Player p, Flag... flags) {
        boolean wait = false;

        for (Flag flag : flags) {
            switch (flag) {
                case NO_PARENTS:
                    RobertStack.clear(p);
                    wait = true;
                    break;
                case REPLACE:
                    RobertStack.phaseDown(p);
                    break;
            }
        }

        if (!wait)
            RobertStack.open(this, p);
        else
            new BukkitRunnable() {
                @Override
                public void run() {
                    RobertStack.open(GUI.this, p);
                }
            }.runTaskLater(Robert.plugin(), 1L);
    }

    /**
     * Closes the GUI and opens a parent if it exists.
     */
    public void close(Player p, Flag... flags) {
        allowClose = true;

        for (Flag flag : flags) {
            switch (flag) {
                case NO_PARENTS:
                    RobertStack.clear(p);
                    break;
                default:
                    break;
            }
        }

        RobertStack.down(p);
    }

    protected void setup(Player p) {
        // If no players have this GUI open, bind the GUI listener.
        if (views.size() == 0) listener.bind();

        views.put(p.getUniqueId(), p.openInventory(inv));
    }

    protected void cleanup(Player p) {
        views.remove(p.getUniqueId());

        // If no more players have this GUI open, unbind it.
        if (views.size() == 0) listener.unbind();
    }

    public void onOpen(Player p, InventoryView view) {
    }

    public void onClose(Player p, InventoryView view) {
    }

    public void onClickPlayerInventory(GUI gui, Player p, InventoryClickEvent event) {
    }
}