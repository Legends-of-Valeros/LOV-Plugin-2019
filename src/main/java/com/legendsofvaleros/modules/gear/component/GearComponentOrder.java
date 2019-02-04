package com.legendsofvaleros.modules.gear.component;

public enum GearComponentOrder {
	DAMAGE(true),
	STATS,
	EXTRA,
	REQUIREMENTS,
	DURABILITY,
	LORE;
	
	final boolean forceSpace;
	
	GearComponentOrder() { this(false); }
	GearComponentOrder(boolean forceSpace) {
		this.forceSpace = forceSpace;
	}
	
	public boolean shouldForceSpace() {
		return forceSpace;
	}
}