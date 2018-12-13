package com.legendsofvaleros.modules.guilds;

import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.core.Characters;

@DependsOn(Characters.class)
public class GuildController extends ModuleListener {
    private static GuildController instance;
    public static GuildController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;
    }
}