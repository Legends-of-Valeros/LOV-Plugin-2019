package com.legendsofvaleros.modules.regions.event;

import com.legendsofvaleros.modules.regions.core.IRegion;
import com.legendsofvaleros.modules.regions.core.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionEnterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player player;

    public Player getPlayer() {
        return player;
    }

    private IRegion region;

    public IRegion getRegion() {
        return region;
    }

    public RegionEnterEvent(Player player, IRegion region) {
        this.player = player;
        this.region = region;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}