package com.legendsofvaleros.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporaryCommand {
	private static final Map<String, Runnable> commands = new HashMap<>();
	public static Runnable get(String id) { return commands.get(id); }
	
	public static String register(Runnable run) {
		String id = UUID.randomUUID().toString();
		commands.put(id, run);
		return id;
	}
	
	public static void unregister(String id) {
		commands.remove(id);
	}
}