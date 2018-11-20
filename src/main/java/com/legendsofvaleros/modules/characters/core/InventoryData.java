package com.legendsofvaleros.modules.characters.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public interface InventoryData {
	ListenableFuture<Void> onInvalidated(PlayerCharacter pc);
	ListenableFuture<Void> saveInventory(PlayerCharacter pc);

	void initInventory(PlayerCharacter pc);
	ListenableFuture<Void> loadInventory(PlayerCharacter pc);

	void onDeath(PlayerCharacter pc);

	String getData();
}