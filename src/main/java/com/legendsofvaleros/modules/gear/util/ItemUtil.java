package com.legendsofvaleros.modules.gear.util;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.component.GearDamage;
import com.legendsofvaleros.modules.gear.component.GearUseSpeed;
import com.legendsofvaleros.modules.gear.component.trigger.PickupTrigger;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.gear.event.InventoryFullEvent;
import com.legendsofvaleros.modules.gear.inventory.ItemListener;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ItemUtil {
	private static Random random = new Random();

	public static int random_int(int min, int max) {
		if(max - min <= 0) return min;
		return random.nextInt(max - min + 1) + min;
	}

	public static double random_double(double min, double max) {
		if(max - min <= 0) return min;
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
	
	public static boolean hasItem(Player p, GearItem gear, int amount) {
		if(amount <= 0) return true;
		
		int count = 0;
		ItemStack[] contents = p.getInventory().getContents();
		GearItem.Instance item;
		// First pass for similar stacking
		for(int i = 9; i < 9 + 9 * 3; i++) {
			if(InventoryManager.hasFixedItem(i)) continue;
			
			item = GearItem.Instance.fromStack(contents[i]);
			if(gear.isSimilar(item)) {
				count += item.amount;
				if(count >= amount)
					break;
			}
		}
		
		return count >= amount;
	}

	public static void dropItem(Location dieLoc, GearItem.Instance instance, PlayerCharacter owner) {
		Item item = dieLoc.getWorld().dropItemNaturally(dieLoc, instance.toStack());
		ItemListener.itemOwner.put(item.getUniqueId(), owner.getPlayerId());
		
		item.setGlowing(true);
		instance.getRarityLevel().getTeam().addEntry(item.getUniqueId().toString());
	}
	
	public static void giveItem(PlayerCharacter pc, GearItem.Instance instance) {
		if(instance == null) return;

		PickupTrigger trigger = new PickupTrigger(pc);
		
		instance.doFire(trigger);
		
		ItemStack stack = instance.toStack();
		if(stack == null) return;
		
		ItemStack[] contents = pc.getPlayer().getInventory().getContents();
		GearItem.Instance item;
		// First pass for similar stacking
		for(int i = 9; i < 9 + 9 * 3; i++) {
			if(InventoryManager.hasFixedItem(i)) continue;
			
			item = GearItem.Instance.fromStack(contents[i]);
			if(instance.gear.isSimilar(item)) {
				if(instance == item)
					instance = instance.copy();

				int newSize = Math.min(item.getMaxAmount(), instance.amount + item.amount);
				instance.amount -= newSize - item.amount;
				item.amount = newSize;
				
				pc.getPlayer().getInventory().setItem(i, item.toStack());
				
				if(instance.amount == 0)
					break;
			}
		}
		for(int i = 9; i < contents.length; i++) {
			if(InventoryManager.hasFixedItem(i)) continue;
			
			item = GearItem.Instance.fromStack(contents[i]);
			if(item == null) {
				pc.getPlayer().getInventory().setItem(i, instance.toStack());
				instance.amount = 0;
				break;
			}
		}
		
		Bukkit.getPluginManager().callEvent(new GearPickupEvent(pc, instance));

		if(instance.amount > 0) {
			InventoryFullEvent event = new InventoryFullEvent(pc, instance);
			Bukkit.getPluginManager().callEvent(event);
			
			if(!event.isCancelled())
				MessageUtil.sendError(pc.getPlayer(), "No more space in inventory! It disappeared!");
		}
	}
	
	public static boolean removeItem(Player p, GearItem gear, int amount) {
		if(gear == null) return false;

		ItemStack[] contents = p.getInventory().getContents();
		GearItem.Instance item;
		for(int i = 9; i < 9 + 9 * 3; i++) {
			if(InventoryManager.hasFixedItem(i)) continue;
			
			item = GearItem.Instance.fromStack(contents[i]);
			if(gear.isSimilar(item)) {
				int newSize = Math.max(0, item.amount - amount);
				amount -= item.amount - newSize;
				item.amount = newSize;
				
				p.getInventory().setItem(i, item.toStack());
				
				if(amount <= 0)
					break;
			}
		}
		
		return amount <= 0;
	}

	public static double getAverageDPS(GearItem.Instance item) {
		GearDamage.Persist pdc = item.getPersist(GearDamage.Component.class);
		if(pdc == null) return 0;
		
		double avg = (pdc.max - pdc.min) / 2D + pdc.min;
		
		GearUseSpeed.Persist usc = item.getPersist(GearUseSpeed.Component.class);
		if(usc != null) avg *= usc.speed;
		
		return avg;
	}

	/*public static double calculateWeaponDamage(CombatEntity attacker) {
		if(attacker == null || attacker.getLivingEntity() == null || attacker.getLivingEntity().getEquipment() == null)
			return 0;

		ItemStack mainHand = attacker.getLivingEntity().getEquipment().getItemInMainHand();
		if(mainHand.getType() == Material.AIR)
			return 0;
		
		Gear heldItem = ItemHandler.toStatItem(mainHand);
		if(heldItem != null)
			if(heldItem.getCurrentDurability() > 0) {
				int damage = heldItem.getMaximumDamage() - heldItem.getMinimumDamage();
				if(damage > 0) {
					attacker.getLivingEntity().getEquipment().setItemInMainHand(ItemHandler.hurtItem(heldItem, 1));
					damage = (heldItem.getMinimumDamage() + new Random().nextInt(heldItem.getMaximumDamage() - heldItem.getMinimumDamage()));
				}
				return damage;
			}
		
		return 0;
	}

	public static double calculateDamageTaken(double damage, CombatEntity defender) {		
		return 0;
	}*/
}