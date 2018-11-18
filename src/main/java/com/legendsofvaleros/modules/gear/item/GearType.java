package com.legendsofvaleros.modules.gear.item;

public enum GearType {
	QUEST_ITEM(false, false),
	JUNK,

	WEAPON,
	ARMOR,
	SHIELD,
	
	ARTIFACT,
	NECKLACE,
	RING,

	SCROLL,
	FOOD,
	POTION,
	CATALYST,
	REAGENT,

	ORE,
	JEWEL,
	
	OTHER;
	
	transient boolean tradable, rarityable;
	
	public boolean isTradable() { return tradable; }
	public boolean isRarityable() { return rarityable; }

	GearType() { this(true, true); }
	GearType(boolean tradable, boolean rarityable) {
		this.tradable = tradable;
		this.rarityable = rarityable;
	}
}