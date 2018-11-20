package com.legendsofvaleros.modules.characters.events;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class PlayerInformationBookEvent extends PlayerCharacterEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	private List<BaseComponent[]> pages = new ArrayList<>();
	public List<BaseComponent[]> getPages() { return pages; }
	public void addPage(BaseComponent[] page) {
		pages.add(page);
	}
	
	public PlayerInformationBookEvent(PlayerCharacter pc) {
		super(pc);
	}
}