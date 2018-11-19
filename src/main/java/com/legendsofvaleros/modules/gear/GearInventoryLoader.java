package com.legendsofvaleros.modules.gear;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.core.PlayerInventoryData;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GearInventoryLoader implements PlayerInventoryData.InventoryMethod {
    @Override
    public String encode(Player p) {
        ItemStack[] contents = p.getInventory().getContents();
        GearItem.Data[] data = new GearItem.Data[contents.length];
        for (int i = 0; i < contents.length; i++) {
            GearItem.Instance instance = GearItem.Instance.fromStack(contents[i]);
            if (instance != null)
                data[i] = instance.getData();
        }
        return ItemManager.gson.toJson(data);
    }

    @Override
    public ItemStack[] decode(String data) {
        try {
            GearItem.Data[] gearData = ItemManager.gson.fromJson(data, GearItem.Data[].class);
            ItemStack[] contents = new ItemStack[gearData.length];
            for (int i = 0; i < contents.length; i++) {
                if (gearData[i] == null) continue;

                ListenableFuture<GearItem> future = GearItem.fromID(gearData[i].id);
                try {
                    GearItem gear = future.get();
                    if (gear != null)
                        contents[i] = gearData[i].toStack();
                } catch (Exception e) {
                    MessageUtil.sendException(Gear.getInstance(), null, e, false);
                    contents[i] = Gear.ERROR_ITEM.newInstance().toStack();
                    contents[i].setAmount(gearData[i].amount);
                }
            }

            return contents;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }
}
