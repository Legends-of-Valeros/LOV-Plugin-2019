package com.legendsofvaleros.util;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@CommandAlias("debug")
public class DebugFlags extends BaseCommand {
	protected static HashMap<UUID, DebugFlags> debug = new HashMap<>();
	public static boolean is(Player p) { return debug.containsKey(p.getUniqueId()); }
	public static DebugFlags get(Player p) {
		if(!debug.containsKey(p.getUniqueId()))
			debug.put(p.getUniqueId(), new DebugFlags());
		return debug.get(p.getUniqueId());
	}

	// Verbose
	public boolean verbose = false;

	@Subcommand("verbose")
	@Description("Toggle verbose logging.")
	@CommandPermission("debug.verbose")
	public void cmdVerbose(Player player) {
		if(get(player).verbose = !get(player).verbose)
			MessageUtil.sendUpdate(player, "Verbose debug enabled.");
		else
			MessageUtil.sendUpdate(player, "Verbose debug disabled.");
	}
	
	// Combat
	public boolean damage = false;

	@Subcommand("damage")
	@Description("Enable damage output.")
	@CommandPermission("debug.damage")
	public void cmdDamage(Player player) {
		if(get(player).damage = !get(player).damage)
			MessageUtil.sendUpdate(player, "Damage debug enabled.");
		else
			MessageUtil.sendUpdate(player, "Damage debug disabled.");
	}
}