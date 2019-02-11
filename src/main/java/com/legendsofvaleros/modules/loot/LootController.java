package com.legendsofvaleros.modules.loot;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.gear.GearController;

@DependsOn(GearController.class)
@ModuleInfo(name = "Loot", info = "")
public class LootController extends Module {
    private static LootController instance;
    public static LootController getInstance() { return instance; }

    private LootAPI api;
    public LootAPI getApi() { return api; }

    @Override
    public void onLoad() {
        super.onLoad();

        this.instance = this;

        this.api = new LootAPI();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();

        try {
            this.api.loadAll().get();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}