package com.legendsofvaleros.modules.regions;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("regions|region")
public class RegionCommands extends BaseCommand {
	@Subcommand("notify")
	@Description("Notify when entering and exiting a region.")
	@CommandPermission("region.notify")
	public void cmdDebug(CommandSender sender) {
		Regions.REGION_DEBUG = !Regions.REGION_DEBUG;
		MessageUtil.sendUpdate(sender, "Region debugging is now " + (Regions.REGION_DEBUG ? "enabled" : "disabled") + ".");
	}

	@Subcommand("wand")
	@Description("Fetch the region wand.")
	@CommandPermission("region.wand")
	public void cmdWand(Player player) {
		player.getInventory().addItem(new ItemBuilder(Material.ARROW).setName(Regions.ITEM_NAME).create());
		MessageUtil.sendUpdate(player, "There you go.");
	}

	@Subcommand("notify")
	@Description("Create a new region. Default access denied.")
	@CommandPermission("region.create")
	@Syntax("<name> [access boolean]")
	public void cmdCreate(Player player, String id, @Optional Boolean access) {
		if(access == null) access = false;

		if(Regions.manager().getRegion(id) != null) {
			MessageUtil.sendError(player, "A region with that ID already exists.");
			return;
		}
		
		Location[] locations = Regions.selection.get(player);
		
		Region region = new Region(id, locations[0].getWorld(), new RegionBounds().setBounds(locations[0], locations[1]));
		region.allowAccess = access;
		Regions.manager().addRegion(region, true);
		MessageUtil.sendUpdate(player, "Region created.");
	}

	@Subcommand("remove")
	@Description("Remove a region.")
	@CommandPermission("region.remove")
	@Syntax("<id>")
	public void cmdRemove(CommandSender sender, String id) {
		if(Regions.manager().getRegion(id) == null) {
			MessageUtil.sendError(sender, "A region with that ID doesn't exist.");
			return;
		}
		
		Regions.manager().removeRegion(id);
		MessageUtil.sendError(sender, "Region removed.");
	}

	@Subcommand("hearthstone")
	@Description("Toggle if a region allows hearthstones.")
	@CommandPermission("region.set.hearthstone")
	@Syntax("<id>")
	public void cmdToggleHearthstone(CommandSender sender, String id) {
		Region region = Regions.manager().getRegion(id);
		if(region == null) {
			MessageUtil.sendError(sender, "A region with that name doesn't exist.");
			return;
		}
		
		region.allowHearthstone = !region.allowHearthstone;
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Allows Hearthstones: " + region.allowHearthstone);
	}

	@Subcommand("enter")
	@Description("Set a region enter message.")
	@CommandPermission("region.set.enter")
	@Syntax("<id> <message>")
	public void cmdSetEnter(CommandSender sender, String id, String message) {
		Region region = Regions.manager().getRegion(id);
		if(region == null) {
			MessageUtil.sendError(sender, "A region with that name doesn't exist.");
			return;
		}
		
		region.msgEnter = message;
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Enter: '" + region.msgEnter + "'");
	}

	@Subcommand("exit")
	@Description("Set a region exit message.")
	@CommandPermission("region.set.exit")
	@Syntax("<id> <message>")
	public void cmdSetExit(CommandSender sender, String id, String message) {
		Region region = Regions.manager().getRegion(id);
		if(region == null) {
			MessageUtil.sendError(sender, "A region with that name doesn't exist.");
			return;
		}

		region.msgExit = message;
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Exit: '" + region.msgExit + "'");
	}

	@Subcommand("failure")
	@Description("Set a region enter failure message.")
	@CommandPermission("region.")
	@Syntax("<id> <message>")
	public void cmdSetFailure(CommandSender sender, String id, String message) {
		Region region = Regions.manager().getRegion(id);
		if(region == null) {
			MessageUtil.sendError(sender, "A region with that name doesn't exist.");
			return;
		}

		region.msgFailure = message;
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Failure: '" + region.msgFailure + "'");
	}

	@Default
	@HelpCommand
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}