package com.legendsofvaleros.modules.zones;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;

@CommandAlias("zones|lov zones")
public class ZoneCommands extends BaseCommand {
	@Subcommand("reload")
	@Description("Reload the zone cache.")
	@CommandPermission("zones.reload")
	public void cmdReload(CommandSender sender) {
		ZonesController.manager().loadZones();
		MessageUtil.sendUpdate(sender, "Zones reloaded.");
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}
