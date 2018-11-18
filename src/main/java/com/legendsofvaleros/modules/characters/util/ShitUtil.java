package com.legendsofvaleros.modules.characters.util;

import java.util.List;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.legendsofvaleros.modules.npcs.util.NPCEmulator;
import com.legendsofvaleros.util.MessageUtil;

public class ShitUtil {
	public static void doShit(Player p, List<String> lines, Runnable complete) {
		doShit(p, lines, 0, 0L, complete);
	}
	
	private static void doShit(Player p, List<String> lines, int i, long delay, Runnable complete) {
		if(i >= lines.size()) {
			if(complete != null)
				complete.run();
			return;
		}

		Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), () -> {
			long ddelay = 0L;
			String[] pair = lines.get(i).trim().split(":");
			switch(pair[0]) {
				case "MSG":
					MessageUtil.sendInfo(p, ChatColor.GRAY + "" + ChatColor.ITALIC + ChatColor.translateAlternateColorCodes('&', pair[1]));
					break;
				case "WAIT":
					ddelay = Long.parseLong(pair[1]);
					break;
				default:
					NPCEmulator.speak(pair[0], p, pair[1]);
					break;
			}
			doShit(p, lines, i + 1, ddelay, complete);
		}, delay);
	}
}