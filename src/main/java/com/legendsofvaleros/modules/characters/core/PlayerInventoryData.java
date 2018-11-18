package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryData implements InventoryData {
	public interface InventoryMethod {
		String encode(Player p);
		ItemStack[] decode(String data);
	}
	
	public static InventoryMethod method = new InventoryMethod() {
		@Override
		public String encode(Player p) {
			return "";
		}

		@Override
		public ItemStack[] decode(String data) {
			return new ItemStack[0];
		}
		
	};
	
	private String inventoryData;
	
	public PlayerInventoryData() { }
	public PlayerInventoryData(String inventoryData) {
		this.inventoryData = inventoryData;
	}
	
	@Override
	public void onInvalidated(PlayerCharacter pc) {
		saveInventory(pc);
		
		pc.getPlayer().getInventory().clear();
	}
	
	@Override
	public void saveInventory(PlayerCharacter pc) {
		inventoryData = method.encode(pc.getPlayer());
	}

	@Override
	public void initInventory(PlayerCharacter pc) {
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, true));
	}

	@Override
	public void loadInventory(PlayerCharacter pc) {
		pc.getPlayer().getInventory().clear();
		
		if(inventoryData != null)
			pc.getPlayer().getInventory().setContents(method.decode(inventoryData));

		Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, false));
	}

	@Override
	public void onDeath(PlayerCharacter pc) {
		
	}
	
	public String getData() {
		return inventoryData;
	}
}