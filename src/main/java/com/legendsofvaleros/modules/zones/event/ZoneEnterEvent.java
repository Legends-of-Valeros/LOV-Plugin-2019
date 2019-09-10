package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.event.HandlerList;

public class ZoneEnterEvent extends PlayerCharacterEvent {
    private static final HandlerList handlers = new HandlerList();

    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public ZoneEnterEvent(PlayerCharacter pc, Zone zone) {
        super(pc);
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