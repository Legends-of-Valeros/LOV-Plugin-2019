package com.legendsofvaleros.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBar {
	public static void set(Player p, String message) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
	}
}