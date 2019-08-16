package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ZoneEnterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player player;

    public Player getPlayer() {
        return player;
    }

    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public ZoneEnterEvent(Player player, Zone zone) {
        this.player = player;
        this.zone = zone;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}