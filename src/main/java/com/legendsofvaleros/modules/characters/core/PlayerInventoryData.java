package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import com.legendsofvaleros.modules.gear.core.Gear;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerInventoryData implements InventoryData {
    private Gear.Data[] inventoryData;

    public PlayerInventoryData() {

    }

    public PlayerInventoryData(Gear.Data[] data) {
        this.inventoryData = data;
    }

    @Override
    public Gear.Data[] getData() {
        return inventoryData;
    }

    @Override
    public void onInvalidated(PlayerCharacter pc) {
        saveInventory(pc);
    }

    @Override
    public void initInventory(PlayerCharacter pc) {
        Characters.getInstance().getScheduler().executeInSpigotCircle(() -> {
            Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, true));
        });
    }

    @Override
    public void saveInventory(PlayerCharacter pc) {
        ItemStack[] contents = pc.getPlayer().getInventory().getContents();

        inventoryData = new Gear.Data[contents.length];
        for (int i = 0; i < contents.length; i++) {
            Gear.Instance instance = Gear.Instance.fromStack(contents[i]);
            if (instance != null)
                inventoryData[i] = instance.getData();
        }
    }

    @Override
    public void loadInventory(PlayerCharacter pc) {
        pc.getPlayer().getInventory().clear();

        if (inventoryData != null) {
            ItemStack[] contents = new ItemStack[inventoryData.length];

            AtomicInteger amount = new AtomicInteger(contents.length);
            for (int i = 0; i < contents.length; i++) {
                if (inventoryData[i] == null) {
                    amount.decrementAndGet();
                    continue;
                }

                contents[i] = inventoryData[i].toStack();
            }

            pc.getPlayer().getInventory().setContents(contents);

            Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, false));
        }
    }

    @Override
    public void onDeath(PlayerCharacter pc) {

    }
}