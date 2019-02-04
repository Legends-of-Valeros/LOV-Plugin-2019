package com.legendsofvaleros.modules.bigbrother;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.ServerMode;
import com.legendsofvaleros.module.Module;

/**
 * Big brother will be the module for centralized event and metrics
 * logging for generating graphs and recreating player actions for
 * moderation.
 */
public class BigBrotherController extends Module {
    public void onLoad() {
        super.onLoad();

        if(LegendsOfValeros.getMode() != ServerMode.LIVE)
            getLogger().info("Disabled outside of a LIVE server. No data will be added to the big brother database.");
    }
}