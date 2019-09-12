package com.legendsofvaleros.modules.npcs.core;

import com.legendsofvaleros.modules.npcs.api.INPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NPCEmulator {
	public static void speak(INPC npc, Player p, String message) {
		speakDirect(npc == null ? null : npc.getName(), p, message);
	}

	private static void speakDirect(String npc, Player p, String message) {
		if(npc == null)
			p.sendMessage(ChatColor.GREEN + "<Invalid NPC>: " + ChatColor.ITALIC + ChatColor.GRAY + message);
		else
			p.sendMessage(ChatColor.GREEN + npc + ": " + ChatColor.RESET + message);
	}
}