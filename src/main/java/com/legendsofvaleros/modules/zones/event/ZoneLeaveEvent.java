package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ZoneLeaveEvent extends PlayerCharacterEvent {
    private static final HandlerList handlers = new HandlerList();

    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public ZoneLeaveEvent(PlayerCharacter pc, Zone zone) {
        super(pc);
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