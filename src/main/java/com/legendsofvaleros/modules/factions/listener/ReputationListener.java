package com.legendsofvaleros.modules.factions.listener;

import com.legendsofvaleros.modules.factions.event.FactionReputationChangeEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ReputationListener implements Listener {
    @EventHandler
    public void onFactionRepChange(FactionReputationChangeEvent event) {
        double percent = (int) (((double) event.getReputation() / event.getFaction().getMaxReputation()) * 1000) / 10D;

        if (event.getChange() > 0) {
            MessageUtil.sendUpdate(event.getPlayer(), event.getFaction().getName() + " reputation rose by " + event.getChange() + ". (" + percent + "%)");
        } else {
            MessageUtil.sendUpdate(event.getPlayer(), event.getFaction().getName() + " reputation dropped by " + event.getChange() + ". (" + percent + "%)");
        }
    }
}