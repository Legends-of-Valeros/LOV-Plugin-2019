package com.legendsofvaleros.module;

import org.bukkit.event.Listener;

public abstract class ModuleListener extends Module implements Listener {

    @Override
    public void onLoad() {
        super.onLoad();

        registerEvents(this);
    }

}
