package com.legendsofvaleros.modules;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class ListenerModule implements Module, Listener {

    @Override
    public void onLoad() {
        Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
    }

}
