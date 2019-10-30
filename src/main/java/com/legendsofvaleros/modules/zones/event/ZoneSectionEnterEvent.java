package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterEvent;
import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.event.HandlerList;

public class ZoneSectionEnterEvent extends PlayerCharacterEvent {
    private static final HandlerList handlers = new HandlerList();

    private Zone.Section section;

    public Zone getZone() {
        return section.getZone();
    }

    public Zone.Section getSection() { return section; }

    public ZoneSectionEnterEvent(PlayerCharacter pc, Zone.Section section) {
        super(pc);
        this.section = section;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}