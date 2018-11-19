package com.legendsofvaleros.modules.loot;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.gear.Gear;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.inventory.ItemStack;

public class LootTable {
    public double chance;
    public Item[] items;

    public transient double totalWeight = Double.MIN_VALUE;

    public static class Item {
        public String id;
        public double weight = 1;

        public ListenableFuture<GearItem> getItem() {
            return GearItem.fromID(id);
        }

        public ListenableFuture<ItemStack> getStack() {
            SettableFuture<ItemStack> ret = SettableFuture.create();

            ListenableFuture<GearItem> future = GearItem.fromID(id);
            future.addListener(() -> {
                try {
                    ItemStack stack = future.get().newInstance().toStack();
                    if (stack != null)
                        ret.set(stack);
                    else
                        LootManager.getInstance().getLogger().severe("Attempt to use loot table item with unknown item name. Offender: " + id);
                } catch (Exception e) {
                    MessageUtil.sendException(LootManager.getInstance(), null, e, false);
                }
            }, Gear.getInstance().getScheduler()::async);

            return ret;
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