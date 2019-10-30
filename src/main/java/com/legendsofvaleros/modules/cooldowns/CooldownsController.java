package com.legendsofvaleros.modules.cooldowns;

import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;

@DependsOn(Characters.class)
@ModuleInfo(name = "Factions", info = "")
public class CooldownsController extends CooldownsAPI {
    private static CooldownsController instance;
    public static CooldownsController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
    }
}