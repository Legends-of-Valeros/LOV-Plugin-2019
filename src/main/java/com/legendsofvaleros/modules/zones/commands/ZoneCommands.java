package com.legendsofvaleros.modules.zones.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;

@CommandAlias("zones|lov zones")
public class ZoneCommands extends BaseCommand {
	@Subcommand("reload")
	@Description("Reload the zone cache.")
	@CommandPermission("zones.reload")
	public void cmdReload(CommandSender sender) {
		ZonesController.getInstance().loadAll();
		MessageUtil.sendUpdate(sender, "Zones reloaded.");
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}
