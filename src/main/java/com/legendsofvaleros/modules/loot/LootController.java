package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.gear.GearController;

@DependsOn(GearController.class)
@ModuleInfo(name = "Loot", info = "")
public class LootController extends LootAPI {
    private static LootController instance;

    public static LootController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
    }
}