package com.legendsofvaleros.modules.characters.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.HandlerList;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;

import mkremins.fanciful.FancyMessage;

public class PlayerInformationBookEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	private List<FancyMessage> pages = new ArrayList<>();
	public List<FancyMessage> getPages() { return pages; }
	public void addPage(FancyMessage page) {
		pages.add(page);
	}
	
	public PlayerInformationBookEvent(PlayerCharacter pc) {
		super(pc);
	}
}