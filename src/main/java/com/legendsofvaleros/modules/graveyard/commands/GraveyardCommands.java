package com.legendsofvaleros.modules.graveyard.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.graveyard.GraveyardController;
import com.legendsofvaleros.modules.graveyard.core.Graveyard;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("graveyards|lov graveyards")
public class GraveyardCommands extends BaseCommand {
	@Subcommand("reload")
	@Description("Reload the graveyard cache.")
	@CommandPermission("graveyards.reload")
	public void cmdReload(CommandSender sender) {
		GraveyardController.getInstance().loadAll();

		MessageUtil.sendUpdate(sender, "Zones reloaded.");
	}

	@Subcommand("create")
	@Description("Create a new graveyard.")
	@CommandPermission("graveyards.create")
	public void cmdCreate(Player player, int radius) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		Zone zone = ZonesController.getInstance().getZone(player);
		if(zone == null) {
			MessageUtil.sendError(player, "You are not within a zone.");
			return;
		}

		Graveyard yard = new Graveyard(zone, player.getLocation(), radius);
		
		GraveyardController.getInstance().addGraveyard(yard);

		MessageUtil.sendUpdate(player, ChatColor.YELLOW + "Created graveyards with radius " + yard.radius + " blocks in zone '" + zone.name + "'.");
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}