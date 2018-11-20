package com.legendsofvaleros.modules.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface IChannelHandler {
	ChatColor getTagColor();
	ChatColor getChatColor();
	boolean canSetDefault();
	boolean canDisable();
	
	String getName(Player p);
	void onChat(Player p, BaseComponent[] bc);
}
