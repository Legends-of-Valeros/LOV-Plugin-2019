package com.legendsofvaleros.modules.skills.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class BindSkillEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	private final int hotbar;
	public int getHotbar() { return hotbar; }
	
	private final int slot;
	public int getSlot() { return slot; }

	private final String skillId;
	public String getSkillId() { return skillId; }
	
	public BindSkillEvent(Player who, int hotbar, int slot, String skillId) {
		super(who);
		
		this.hotbar = hotbar;
		this.slot = slot;
		this.skillId = skillId;
	}
}