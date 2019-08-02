package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Crystall on 04/07/2019
 * Gets fired whenever a Zone is getting activated
 */
public class ZoneActivateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public ZoneActivateEvent(Zone zone) {
        this.zone = zone;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}