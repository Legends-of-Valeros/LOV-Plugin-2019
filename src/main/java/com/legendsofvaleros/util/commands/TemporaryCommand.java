package com.legendsofvaleros.util.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("tmp")
public class TemporaryCommand extends BaseCommand {
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

	@Default
	private void cmdTmp(CommandSender sender, String id) {
		Runnable runnable = TemporaryCommand.get(id);
		if(runnable == null) return;
		runnable.run();
	}
}