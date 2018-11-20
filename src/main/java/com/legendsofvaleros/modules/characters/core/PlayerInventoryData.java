package com.legendsofvaleros.modules.characters.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterInventoryFillEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

public class PlayerInventoryData implements InventoryData {
	public interface InventoryMethod {
		ListenableFuture<String> encode(ItemStack[] contents);
		ListenableFuture<ItemStack[]> decode(String data);
	}
	
	public static InventoryMethod method = new InventoryMethod() {
		@Override
		public ListenableFuture<String> encode(ItemStack[] contents) {
			SettableFuture<String> ret = SettableFuture.create();
			ret.set("");
			return ret;
		}

		@Override
		public ListenableFuture<ItemStack[]> decode(String data) {
			SettableFuture<ItemStack[]> ret = SettableFuture.create();
			ret.set(new ItemStack[0]);
			return ret;
		}
		
	};
	
	private String inventoryData;
	
	public PlayerInventoryData() { }
	public PlayerInventoryData(String inventoryData) {
		this.inventoryData = inventoryData;
	}
	
	@Override
	public ListenableFuture<Void> onInvalidated(PlayerCharacter pc) {
		SettableFuture<Void> ret = SettableFuture.create();

		saveInventory(pc).addListener(() -> {
			pc.getPlayer().getInventory().clear();

			ret.set(null);
		}, Characters.getInstance().getScheduler()::sync);

		return ret;
	}
	
	@Override
	public ListenableFuture<Void> saveInventory(PlayerCharacter pc) {
		SettableFuture<Void> ret = SettableFuture.create();

		ListenableFuture<String> future = method.encode(pc.getPlayer().getInventory().getContents());
		future.addListener(() -> {
			try {
				inventoryData = future.get();

				ret.set(null);
			} catch(InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}, Characters.getInstance().getScheduler()::async);

		return ret;
	}

	@Override
	public void initInventory(PlayerCharacter pc) {
		Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, true));
	}

	@Override
	public ListenableFuture<Void> loadInventory(PlayerCharacter pc) {
		SettableFuture<Void> ret = SettableFuture.create();

		pc.getPlayer().getInventory().clear();
		
		if(inventoryData != null) {
			ListenableFuture<ItemStack[]> future = method.decode(inventoryData);
			future.addListener(() -> {
				try {
					pc.getPlayer().getInventory().setContents(future.get());

					Bukkit.getServer().getPluginManager().callEvent(new PlayerCharacterInventoryFillEvent(pc, false));

					ret.set(null);
				} catch(InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}, Characters.getInstance().getScheduler()::sync);
		}

		return ret;
	}

	@Override
	public void onDeath(PlayerCharacter pc) {
		
	}
	
	public String getData() {
		return inventoryData;
	}
}