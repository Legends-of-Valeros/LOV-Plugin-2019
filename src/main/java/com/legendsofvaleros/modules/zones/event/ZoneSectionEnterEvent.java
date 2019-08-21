package com.legendsofvaleros.modules.zones.event;

import com.legendsofvaleros.modules.zones.core.Zone;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ZoneSectionEnterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player player;

    public Player getPlayer() {
        return player;
    }

    private Zone.Section section;

    public Zone getZone() {
        return section.getZone();
    }

    public Zone.Section getSection() { return section; }

    public ZoneSectionEnterEvent(Player player, Zone.Section section) {
        this.player = player;
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