package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import org.bukkit.event.HandlerList;

/**
 * An event called when a player's character data has finished loading and they are allowed to start
 * playing as that character.
 * <p>
 * In many cases, this even can and should replace <code>PlayerJoinEvent</code> as an event that
 * signals a the point at which a player is first allowed to do things on the server.
 * <p>
 * Not called if a player logs out during their loading process.
 */
public class PlayerCharacterFinishLoadingEvent extends PlayerCharacterEvent {

	private static final HandlerList handlers = new HandlerList();

	private final boolean firstLogin;

	public PlayerCharacterFinishLoadingEvent(PlayerCharacter playerCharacter, boolean firstLogin) {
		super(playerCharacter);
		this.firstLogin = firstLogin;
	}

	public boolean isFirstLogin() {
		return firstLogin;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
