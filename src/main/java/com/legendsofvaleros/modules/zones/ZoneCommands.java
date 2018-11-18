package com.legendsofvaleros.modules.zones;

import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.command.CommandSender;

import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;

public class ZoneCommands {
	@CommandManager.Cmd(cmd = "zones reload", help = "Reload the zone cache.", permission = "zones.reload")
	public static CommandManager.CommandFinished cmdReload(CommandSender sender, Object[] args) {
		Zones.manager().loadZones();
		sender.sendMessage("Zones reloaded.");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "zones", help = "List all zones.", permission = "zones.list")
	public static CommandManager.CommandFinished cmdList(CommandSender sender, Object[] args) {
		sender.sendMessage("Zones:");
		for(Zone zone : Zones.manager().getZones())
			sender.sendMessage(" -" + zone.material.name() + (zone.materialData > 0 ? ":" + zone.materialData : "") + " = " + zone.name);
		return CommandManager.CommandFinished.DONE;
	}
}
