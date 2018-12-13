package com.legendsofvaleros.modules.guilds;

import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.zones.Zones;

@DependsOn(Characters.class)
public class Guilds extends ModuleListener {
    private static Guilds inst;
    public static Guilds getInstance() {
        return inst;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.inst = this;
    }
}