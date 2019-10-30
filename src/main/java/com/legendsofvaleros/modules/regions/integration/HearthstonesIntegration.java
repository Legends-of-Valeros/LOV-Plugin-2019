package com.legendsofvaleros.modules.regions.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.hearthstones.event.HearthstoneCastEvent;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.regions.core.IRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HearthstonesIntegration extends Integration implements Listener {
    public HearthstonesIntegration() {
        RegionController.getInstance().registerEvents(this);
    }

    @EventHandler
    public void onCastHearthstone(HearthstoneCastEvent event) {
        for (IRegion region : RegionController.getInstance().getPlayerRegions(event.getPlayer())) {
            if (!region.areHearthstonesAllowed()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
