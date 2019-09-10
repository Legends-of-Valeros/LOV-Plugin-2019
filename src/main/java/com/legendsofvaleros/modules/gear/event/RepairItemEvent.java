package com.legendsofvaleros.modules.gear.event;

import com.legendsofvaleros.modules.gear.core.Gear;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RepairItemEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Gear.Instance gearInstance;

    public Gear.Instance getItem() {
        return gearInstance;
    }

    public RepairItemEvent(Player who, Gear.Instance gearInstance) {
        super(who);
        this.gearInstance = gearInstance;
    }
}