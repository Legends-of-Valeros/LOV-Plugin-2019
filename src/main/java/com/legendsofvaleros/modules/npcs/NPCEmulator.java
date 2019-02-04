package com.legendsofvaleros.modules.npcs;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NPCEmulator {
	public static void speak(String npcId, Player p, String message) {
		speakDirect(NPCsController.getNPC(npcId).name, p, message);
	}

	public static void speak(NPC npc, Player p, String message) {
		speakDirect(npc == null ? null : npc.getName(), p, message);
	}

	private static void speakDirect(String npc, Player p, String message) {
		if(npc == null)
			p.sendMessage(ChatColor.GREEN + p.getName() + ": " + ChatColor.ITALIC + ChatColor.GRAY + message);
		else
			p.sendMessage(ChatColor.GREEN + npc + ": " + ChatColor.RESET + message);
	}
}