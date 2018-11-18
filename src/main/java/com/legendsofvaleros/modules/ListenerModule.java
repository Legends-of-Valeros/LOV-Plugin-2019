package com.legendsofvaleros.modules;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class ListenerModule extends Module implements Listener {

    @Override
    public void onLoad() {
        super.onLoad();
        Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
    }

}
