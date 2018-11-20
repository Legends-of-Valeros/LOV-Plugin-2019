package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.HandlerList;

public class PlayerCharacterCreateEvent extends PlayerCharacterEvent {

	private static final HandlerList handlers = new HandlerList();

	public PlayerCharacterCreateEvent(PlayerCharacter playerCharacter) {
		super(playerCharacter);
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
