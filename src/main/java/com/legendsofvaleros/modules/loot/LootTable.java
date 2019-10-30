package com.legendsofvaleros.modules.loot;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.loot.api.ILootTable;

public class LootTable implements ILootTable {
    @SerializedName("_id")
    private String id;
    private String slug;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
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