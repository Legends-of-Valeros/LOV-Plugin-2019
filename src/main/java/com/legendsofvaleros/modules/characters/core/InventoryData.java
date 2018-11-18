package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public interface InventoryData {
	void onInvalidated(PlayerCharacter pc);
	void saveInventory(PlayerCharacter pc);

	void initInventory(PlayerCharacter pc);
	void loadInventory(PlayerCharacter pc);

	void onDeath(PlayerCharacter pc);

	String getData();
}