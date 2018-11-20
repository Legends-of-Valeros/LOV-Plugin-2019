package com.legendsofvaleros.modules.pvp.duel.component;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.pvp.duel.Duel;
import com.legendsofvaleros.modules.pvp.duel.DuelTeam;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Stakes extends DuelComponent {

    private List<ItemStack> stakes;

    public Stakes(List<ItemStack> stakes) {
        this.stakes = stakes;
    }

    public List<ItemStack> getStakes() {
        return stakes;
    }

    @Override
    public void handleVictory(Duel duel, DuelTeam victors) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Rewards");
        inventory.setContents(stakes.toArray(new ItemStack[1]));

        for (PlayerCharacter playerCharacter : victors.getTeamMembers()) {
            playerCharacter.getPlayer().openInventory(inventory);
        }
    }
}
