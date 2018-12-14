package com.legendsofvaleros.modules.gear;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.gear.item.Gear;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class GearInventoryLoader implements PlayerInventoryData.InventoryMethod {
    @Override
    public ListenableFuture<String> encode(ItemStack[] contents) {
        SettableFuture<String> ret = SettableFuture.create();

        Gear.Data[] data = new Gear.Data[contents.length];
        for (int i = 0; i < contents.length; i++) {
            Gear.Instance instance = Gear.Instance.fromStack(contents[i]);
            if (instance != null)
                data[i] = instance.getData();
        }
        ret.set(ItemManager.gson.toJson(data));

        return ret;
    }

    @Override
    public ListenableFuture<ItemStack[]> decode(String data) {
        SettableFuture<ItemStack[]> ret = SettableFuture.create();

        Gear.Data[] gearData = ItemManager.gson.fromJson(data, Gear.Data[].class);
        ItemStack[] contents = new ItemStack[gearData.length];

        AtomicInteger amount = new AtomicInteger(contents.length);
        for (int i = 0; i < contents.length; i++) {
            if (gearData[i] == null) {
                amount.decrementAndGet();
                continue;
            }

            contents[i] = gearData[i].toStack();
        }

        ret.set(contents);

        return ret;
    }
}
