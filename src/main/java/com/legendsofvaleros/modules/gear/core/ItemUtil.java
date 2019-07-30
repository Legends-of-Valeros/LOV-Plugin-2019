package com.legendsofvaleros.modules.gear.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.component.core.GearPhysicalDamage;
import com.legendsofvaleros.modules.gear.component.core.GearUseSpeed;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.InventoryFullEvent;
import com.legendsofvaleros.modules.gear.listener.ItemListener;
import com.legendsofvaleros.modules.gear.trigger.PickupTrigger;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ItemUtil {
    private static Random random = new Random();

    public static int random_int(int min, int max) {
        if (max - min <= 0) return min;
        return random.nextInt(max - min + 1) + min;
    }

    public static double random_double(double min, double max) {
        if (max - min <= 0) return min;
        return (min + (max - min) * random.nextDouble());
    }

    public static String getOperatorSign(double value) {
        if (value < 0) return ChatColor.RED + "-";
        if (value > 0) return ChatColor.GREEN + "+";
        return " ";
    }

    public static boolean random_bool() {
        return random.nextBoolean();
    }

    public static boolean hasItem(Player p, Gear gear, int amount) {
        if (amount <= 0) return true;

        int count = 0;
        ItemStack[] contents = p.getInventory().getContents();
        Gear.Instance item;
        // First pass for similar stacking
        for (int i = 9; i < 9 + 9 * 3; i++) {
            if (InventoryManager.hasFixedItem(i)) continue;

            item = Gear.Instance.fromStack(contents[i]);
            if (gear.isSimilar(item)) {
                count += item.amount;
                if (count >= amount)
                    break;
            }
        }

        return count >= amount;
    }

    /**
     * Is an item equivalent to not existing?
     * @param itemStack
     * @return isAir
     */
    public static boolean isAir(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public static void dropItem(Location dieLoc, Gear.Instance instance, PlayerCharacter owner) {
        Item item = dieLoc.getWorld().dropItemNaturally(dieLoc, instance.toStack());
        ItemListener.itemOwner.put(item.getUniqueId(), owner.getPlayerId());

        item.setGlowing(true);
        instance.getRarityLevel().getTeam().addEntry(item.getUniqueId().toString());
    }

    /**
     * Remove an ItemStack from the player's inventory.
     * @param player
     * @param gear
     * @return wasRemoved
     */
    public static boolean removeItem(Player player, Gear.Instance gear) {
        ItemStack itemStack = gear.toStack();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            // Check if a fixed item is on the current slot
            if (InventoryManager.hasFixedItem(i)) {
                continue;
            }

            ItemStack itm = player.getInventory().getItem(i);
            if (itemStack.equals(itm)) {
                itm.setAmount(itm.getAmount() - itemStack.getAmount());
                if (itm.getAmount() <= 0) {
                    itm.setType(Material.AIR);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Give an item to a player if their inventory is not full.
     * @param pc
     * @param instance
     * @return isDropped
     */
    public static boolean giveItem(PlayerCharacter pc, Gear.Instance instance) {
        if (instance == null) {
            return false;
        }

        Player player = pc.getPlayer();
        PickupTrigger trigger = new PickupTrigger(pc);
        instance.doFire(trigger);
        ItemStack itemStack = instance.toStack();

        if (isAir(itemStack)) {
            return false; // Can't give empty item.
        }


        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) { // Add the possible items to the player's inventory.
            ItemStack item = inv.getItem(i);
            if (isAir(item) || !item.isSimilar(itemStack) || InventoryManager.hasFixedItem(i)) {
                continue;
            }

            int deposit = Math.min(item.getMaxStackSize(), item.getAmount() + itemStack.getAmount()) - item.getAmount();
            if (item.getAmount() + deposit > item.getMaxStackSize()) {
                continue; //respect the custom max amount of custom items
            }
            itemStack.setAmount(itemStack.getAmount() - deposit);
            item.setAmount(item.getAmount() + deposit);
        }

        if (itemStack.getAmount() == 0) {
            Bukkit.getPluginManager().callEvent(new GearPickupEvent(pc, instance));
            return true;
        }

        if (inv.firstEmpty() == -1) {
            MessageUtil.sendError(player, "Your inventory is full. Dropping item on the ground ...");
            dropItem(player.getLocation(), instance, pc);
            Bukkit.getPluginManager().callEvent(new InventoryFullEvent(pc, instance));
            return false;
        }

        // There is an open stack, we can add it with bukkit.
        inv.addItem(itemStack);
        Bukkit.getPluginManager().callEvent(new GearPickupEvent(pc, instance));

        return true;
    }

    public static double getAverageDPS(Gear.Instance item) {
        GearPhysicalDamage.Persist pdc = item.getPersist(GearPhysicalDamage.Component.class);
        if (pdc == null) return 0;

        double avg = (pdc.max - pdc.min) / 2D + pdc.min;

        GearUseSpeed.Persist usc = item.getPersist(GearUseSpeed.Component.class);
        if (usc != null) avg *= usc.speed;

        return avg;
    }

}