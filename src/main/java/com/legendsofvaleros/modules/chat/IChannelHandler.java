package com.legendsofvaleros.modules.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mkremins.fanciful.FancyMessage;

public interface IChannelHandler {
	ChatColor getTagColor();
	ChatColor getChatColor();
	boolean canSetDefault();
	boolean canDisable();
	
	String getName(Player p);
	void onChat(Player p, FancyMessage fm);
}
