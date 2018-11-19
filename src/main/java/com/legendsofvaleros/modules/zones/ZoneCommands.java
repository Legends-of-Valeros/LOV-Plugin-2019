package com.legendsofvaleros.modules.zones;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.command.CommandSender;

import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;

@CommandAlias("zones|zone")
public class ZoneCommands extends BaseCommand {
	@Subcommand("reload")
	@Description("Reload the zone cache.")
	@CommandPermission("zones.reload")
	public void cmdReload(CommandSender sender) {
		Zones.manager().loadZones();
		MessageUtil.sendUpdate(sender, "Zones reloaded.");
	}

	@Default
	@HelpCommand
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}
