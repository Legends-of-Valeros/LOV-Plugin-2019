package com.legendsofvaleros.modules;

import org.bukkit.event.Listener;

public abstract class ListenerModule extends Module implements Listener {

    @Override
    public void onLoad() {
        super.onLoad();

        registerEvents(this);
    }

}
