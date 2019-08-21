package com.legendsofvaleros.modules.characters.core;

import com.legendsofvaleros.api.Promise;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public interface InventoryData {
	Promise onInvalidated(PlayerCharacter pc);
	Promise saveInventory(PlayerCharacter pc);

	void initInventory(PlayerCharacter pc);
	Promise loadInventory(PlayerCharacter pc);

	void onDeath(PlayerCharacter pc);

	String getData();
}