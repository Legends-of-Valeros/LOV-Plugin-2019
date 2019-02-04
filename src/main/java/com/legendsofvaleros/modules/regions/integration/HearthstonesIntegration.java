package com.legendsofvaleros.modules.regions.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.hearthstones.event.HearthstoneCastEvent;
import com.legendsofvaleros.modules.regions.RegionController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HearthstonesIntegration extends Integration implements Listener {
    public HearthstonesIntegration() {
        RegionController.getInstance().registerEvents(this);
    }

    @EventHandler
    public void onCastHearthstone(HearthstoneCastEvent event) {
        for (String region_id : RegionController.manager().getPlayerRegions(event.getPlayer())) {
            if (!RegionController.manager().getRegion(region_id).allowHearthstone) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
