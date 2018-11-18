package com.legendsofvaleros.modules.characters.events;

import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

public class PlayerCharacterRemoveEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();

	public PlayerCharacterRemoveEvent(PlayerCharacter pc) {
		super(pc);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
