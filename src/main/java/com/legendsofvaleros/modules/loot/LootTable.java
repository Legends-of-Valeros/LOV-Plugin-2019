package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootTable {
    public double chance;
    public Item[] items;

    public transient double totalWeight = Double.MIN_VALUE;

    public static class Item {
        public String id;
        public double weight = 1;

        public Gear getItem() {
            return Gear.fromID(id);
        }

        public ItemStack getStack() {
            ItemStack stack = Gear.fromID(id).newInstance().toStack();
            if (stack.getType() != Material.AIR)
                return stack;
            else
                LootManager.getInstance().getLogger().severe("Attempt to use loot table item with unknown item name. Offender: " + id);
            return null;
        }

        @Override
        public String toString() {
            return "LootTable(id=" + id + ", weight=" + weight + ")";
        }
    }

    public Item nextItem() {
        if (totalWeight == Double.MIN_VALUE) {
            totalWeight = 0D;
            for (Item i : items)
                totalWeight += i.weight;
        }

        int index = -1;
        double random = Math.random() * totalWeight;
        for (int i = 0; i < items.length; ++i) {
            random -= items[i].weight;
            if (random <= 0D) {
                index = i;
                break;
            }
        }

        return items[index];
    }
}