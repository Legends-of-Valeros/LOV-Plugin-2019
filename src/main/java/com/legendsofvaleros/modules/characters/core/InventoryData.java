package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public interface InventoryData {
	Promise<Boolean> onInvalidated(PlayerCharacter pc);
	Promise<Boolean> saveInventory(PlayerCharacter pc);

	void initInventory(PlayerCharacter pc);
	Promise<Boolean> loadInventory(PlayerCharacter pc);

	void onDeath(PlayerCharacter pc);

	String getData();
}