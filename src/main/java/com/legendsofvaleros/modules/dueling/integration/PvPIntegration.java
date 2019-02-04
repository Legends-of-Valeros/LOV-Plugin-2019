package com.legendsofvaleros.modules.dueling.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.dueling.core.Duel;
import com.legendsofvaleros.modules.dueling.DuelingController;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PvPIntegration extends Integration {
    private DuelingController dueling = DuelingController.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void isPvPAllowed(PvPCheckEvent event) {
        // A duel should override every other PvP setting. Tis a fight
        // to the death, regardless of kinship.

        Duel d = dueling.getDuel(event.getAttacker(), event.getDamaged());
        if(d == null) {
            // If either player is in a duel, cancel damage.
            if(dueling.getDuel(event.getAttacker()) != null || dueling.getDuel(event.getDamaged()) != null)
                event.setCancelled(true);
            return;
        }

        // These two players are dueling. Allow PvP.
        event.setCancelled(false);
    }
}
