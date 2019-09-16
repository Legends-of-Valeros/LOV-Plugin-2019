package com.legendsofvaleros.features.gui.slot;

import org.bukkit.inventory.ItemStack;

public class Slot {
    public final ItemStack stack;
    public final ISlotAction action;

    public Slot(ItemStack stack, ISlotAction action) {
        this.stack = stack;
        this.action = action;
    }
}