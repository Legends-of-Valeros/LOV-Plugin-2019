package com.legendsofvaleros.modules.loot.api;

import com.legendsofvaleros.modules.gear.core.Gear;

public interface ILootTable {
    String getId();

    Gear nextItem();
}