package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.modules.gear.core.Gear;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootTable {
    public String id;
    public Item[] items;

    public transient double totalWeight = Double.MIN_VALUE;

    public static class Item {
        public String id;
        public double weight = 1;

        public Gear getItem() {
            return Gear.fromId(id);
        }

        @Override
        public String toString() {
            return "LootTable(id=" + id + ", weight=" + weight + ")";
        }
    }

    public Gear nextItem() {
        if (totalWeight == Double.MIN_VALUE) {
            totalWeight = 0D;
            for (Item i : items) {
                totalWeight += i.weight;
            }
        }

        int index = - 1;
        double random = Math.random() * totalWeight;
        for (int i = 0; i < items.length; ++ i) {
            random -= items[i].weight;
            if (random <= 0D) {
                index = i;
                break;
            }
        }

        return items[index].getItem();
    }
}