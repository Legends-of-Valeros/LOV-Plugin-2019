package com.legendsofvaleros.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Moustache {
	private static Map<String, IMoustache> replacements = new HashMap<>();
	public static void register(String tag, IMoustache replace) { replacements.put(tag, replace); }
	
	public static String translate(Player p, String str) {
		str = str.replaceAll("\\{\\{name\\}\\}", p.getName());
		return str;
	}
	
	@FunctionalInterface
	public interface IMoustache {
		String replace(Player p);
	}
}