package com.legendsofvaleros.util;

import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class DebugFlags {
	protected static HashMap<UUID, DebugFlags> debug = new HashMap<>();
	public static boolean is(Player p) { return debug.containsKey(p.getUniqueId()); }
	public static DebugFlags get(Player p) {
		if(!debug.containsKey(p.getUniqueId()))
			debug.put(p.getUniqueId(), new DebugFlags());
		return debug.get(p.getUniqueId());
	}
	
	public boolean verbose = false;
	@CommandManager.Cmd(cmd = "verbose", help = "Enable damage output.", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdVerbose(CommandSender sender, Object[] args) {
		if(get((Player)sender).verbose = !get((Player)sender).verbose)
			MessageUtil.sendUpdate(sender, "Verbose debug enabled.");
		else
			MessageUtil.sendUpdate(sender, "Verbose debug disabled.");
		return CommandManager.CommandFinished.DONE;
	}
	
	// Combat
	public boolean damage = false;
	@CommandManager.Cmd(cmd = "damage", help = "Enable damage output.", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdDamage(CommandSender sender, Object[] args) {
		if(get((Player)sender).damage = !get((Player)sender).damage)
			MessageUtil.sendUpdate(sender, "Damage debug enabled.");
		else
			MessageUtil.sendUpdate(sender, "Damage debug disabled.");
		return CommandManager.CommandFinished.DONE;
	}
}