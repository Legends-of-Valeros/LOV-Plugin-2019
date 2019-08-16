package com.legendsofvaleros.modules.hotswitch.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerSwitchHotbarEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Override
	public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private int hotbar;

    public int getCurrentHotbar() {
        return hotbar;
    }

    public void setCurrentHotbar(int i) {
        this.hotbar = i;
    }

    public PlayerSwitchHotbarEvent(Player player, int hotbar) {
        super(player);
        this.hotbar = hotbar;
    }
}