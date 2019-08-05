package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ZoneDeactivateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public ZoneDeactivateEvent(Zone zone) {
        this.zone = zone;
    }

    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}