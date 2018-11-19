package com.legendsofvaleros.modules.characters.util;

import com.legendsofvaleros.util.Utilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Used to capture player messages until told to stop. Prevents messages from being sent in chat.
 */
public abstract class CapturePlayerChat implements Listener {
	private Player captureFrom;
	
	public CapturePlayerChat(Player p) {
		captureFrom = p;

		Utilities.getInstance().registerEvents(this);
	}
	
	/**
	 * The captured message.
	 * 
	 * @param p Instance of the captured player.
	 * @param message The message the player sent.
	 * @return If this is true, it stops capturing
	 */
	public abstract boolean onMessage(Player p, String message);
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(event.getPlayer() != captureFrom)
			return;
		
		onMessage(event.getPlayer(), event.getMessage());
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
		event.setCancelled(true);
	}
}