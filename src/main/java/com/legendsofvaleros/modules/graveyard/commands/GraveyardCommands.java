package com.legendsofvaleros.modules.graveyard.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.graveyard.Graveyard;
import com.legendsofvaleros.modules.graveyard.GraveyardManager;
import com.legendsofvaleros.modules.zones.Zone;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("graveyards|lov graveyards")
public class GraveyardCommands extends BaseCommand {
	@Subcommand("create")
	@Description("Create a new graveyard.")
	@CommandPermission("graveyards.create")
	public void cmdCreate(Player player, int radius) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		Zone zone = ZonesController.getManager().getZone(player);
		if(zone == null) {
			MessageUtil.sendError(player, "You are not within a zone.");
			return;
		}
		
		Graveyard data = GraveyardManager.create(zone, player.getLocation().getWorld(),
														player.getLocation().getBlockX(),
														player.getLocation().getBlockY(),
														player.getLocation().getBlockZ(),
														radius);

		MessageUtil.sendUpdate(player, ChatColor.YELLOW + "Created graveyards with radius " + data.radius + " blocks in zone '" + zone.name + "'.");
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}