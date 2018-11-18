package com.legendsofvaleros.modules.graveyard;

import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.util.cmd.CommandManager.Arg;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.modules.zones.Zone;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GraveyardCommands {
	@CommandManager.Cmd(cmd = "graveyards create", args = "<radius>", argTypes = { CommandManager.Arg.ArgInteger.class }, help = "Create a graveyards.", longhelp = "Create a graveyards at your current location with radius.", permission = "graveyards.create", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdCreate(CommandSender sender, Object[] args) {
		Zone zone = Zones.manager().getZone((Player)sender);
		if(zone == null)
			return CommandManager.CommandFinished.CUSTOM.replace("You are not within a zone.");
		
		Graveyard data = GraveyardManager.create(zone, ((Player)sender).getLocation().getWorld(), ((Player)sender).getLocation().getBlockX(), ((Player)sender).getLocation().getBlockY(), ((Player)sender).getLocation().getBlockZ());
		
		data.radius = (Integer)args[0];
		sender.sendMessage(ChatColor.YELLOW + "Created graveyards with radius " + data.radius + " blocks in zone '" + zone.name + "'.");

		return CommandManager.CommandFinished.DONE;
	}
}