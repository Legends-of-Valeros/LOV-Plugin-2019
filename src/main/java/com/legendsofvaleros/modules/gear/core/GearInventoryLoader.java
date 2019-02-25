package com.legendsofvaleros.modules.gear.core;

import com.legendsofvaleros.api.APIController;
import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class GearInventoryLoader implements PlayerInventoryData.InventoryMethod {
    @Override
    public Promise<String> encode(ItemStack[] contents) {
        return Promise.make(() -> {
            Gear.Data[] data = new Gear.Data[contents.length];
            for (int i = 0; i < contents.length; i++) {
                Gear.Instance instance = Gear.Instance.fromStack(contents[i]);
                if (instance != null)
                    data[i] = instance.getData();
            }

            return APIController.getInstance().getGson().toJson(data);
        });
    }

    @Override
    public Promise<ItemStack[]> decode(String data) {
        return Promise.make(() -> {
            Gear.Data[] gearData = APIController.getInstance().getGson().fromJson(data, Gear.Data[].class);
            ItemStack[] contents = new ItemStack[gearData.length];

            AtomicInteger amount = new AtomicInteger(contents.length);
            for (int i = 0; i < contents.length; i++) {
                if (gearData[i] == null) {
                    amount.decrementAndGet();
                    continue;
                }

                contents[i] = gearData[i].toStack();
            }

            return contents;
        });
    }
}
