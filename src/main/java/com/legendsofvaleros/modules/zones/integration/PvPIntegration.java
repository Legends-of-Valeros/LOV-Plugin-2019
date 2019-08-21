package com.legendsofvaleros.modules.zones.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PvPIntegration extends Integration implements Listener {
    public PvPIntegration() {
        ZonesController.getInstance().registerEvents(this);
    }

    @EventHandler
    public void isPvPAllowed(PvPCheckEvent event) {
        // Zones should never override a PvP check.
        if(event.isCancelled()) return;

        Zone.Section aZone = ZonesController.getInstance().getZone(event.getAttacker());
        Zone.Section dZone = ZonesController.getInstance().getZone(event.getDamaged());

        if((aZone != null && !aZone.pvp)
                || (dZone != null && !dZone.pvp)) {
            event.setCancelled(true);
        }
    }
}
