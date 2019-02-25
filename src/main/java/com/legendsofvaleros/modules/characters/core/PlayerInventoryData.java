package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryData implements InventoryData {
	public interface InventoryMethod {
		Promise<String> encode(ItemStack[] contents);
		Promise<ItemStack[]> decode(String data);
	}
	
	public static InventoryMethod method = new InventoryMethod() {
		@Override
		public Promise<String> encode(ItemStack[] contents) {
			return Promise.make("");
		}

		@Override
		public Promise<ItemStack[]> decode(String data) {
			return Promise.make(new ItemStack[0]);
		}
		
	};
	
	private String inventoryData;
	
	public PlayerInventoryData() { }
	public PlayerInventoryData(String inventoryData) {
		this.inventoryData = inventoryData;
	}
	
	@Override
	public Promise<Boolean> onInvalidated(PlayerCharacter pc) {
		return saveInventory(pc);
	}
	
	@Override
	public Promise<Boolean> saveInventory(PlayerCharacter pc) {
		return method.encode(pc.getPlayer().getInventory().getContents()).onSuccess(val -> {
			inventoryData = val.orElseGet(null);
		}).next(val -> Promise.make(val.isPresent()));
	}

	@Override
	public void initInventory(PlayerCharacter pc) {
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, true));
	}

	@Override
	public Promise<Boolean> loadInventory(PlayerCharacter pc) {
		pc.getPlayer().getInventory().clear();

		if(inventoryData != null) {
			return method.decode(inventoryData).onSuccess(val -> {
				pc.getPlayer().getInventory().setContents(val.orElse(new ItemStack[0]));

				Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, false));
			}, Characters.getInstance().getScheduler()::sync)
					.next(al -> Promise.make(al.isPresent()));
		}

		return Promise.make(true);
	}

	@Override
	public void onDeath(PlayerCharacter pc) {
		
	}
	
	public String getData() {
		return inventoryData;
	}
}