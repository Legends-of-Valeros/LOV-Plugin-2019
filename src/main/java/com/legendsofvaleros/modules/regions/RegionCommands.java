package com.legendsofvaleros.modules.regions;

import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.cmd.CommandManager.Arg;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegionCommands {
	@CommandManager.Cmd(cmd = "region notify", help = "Notify when entering and exiting a region.", permission = "region.notify")
	public static CommandManager.CommandFinished cmdDebug(CommandSender sender, Object[] args) {
		Regions.REGION_DEBUG = !Regions.REGION_DEBUG;
		MessageUtil.sendUpdate(sender, "Region debugging is now " + (Regions.REGION_DEBUG ? "enabled" : "disabled") + ".");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region wand", help = "Fetch the region wand.", permission = "region.wand", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdWand(CommandSender sender, Object[] args) {
		((Player)sender).getInventory().addItem(new ItemBuilder(Material.ARROW).setName(Regions.ITEM_NAME).create());
		MessageUtil.sendUpdate(sender, "There you go.");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region create", args = "<name> [access boolean]", argTypes = { CommandManager.Arg.ArgString.class, CommandManager.Arg.ArgBoolean.class }, help = "Create a new region. Default access denied.", permission = "region.create")
	public static CommandManager.CommandFinished cmdCreate(CommandSender sender, Object[] args) {
		if(Regions.manager().getRegion((String)args[0]) != null)
			return CommandManager.CommandFinished.CUSTOM.replace("A region with that name already exists.");
		
		Location[] locations = Regions.selection.get(sender);
		
		Region region = new Region((String)args[0], locations[0].getWorld(), new RegionBounds().setBounds(locations[0], locations[1]));
		region.allowAccess = (Boolean)args[1];
		Regions.manager().addRegion(region, true);
		MessageUtil.sendUpdate(sender, "Region created.");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region remove", args = "<name>", help = "Remove a region.", permission = "region.remove")
	public static CommandManager.CommandFinished cmdRemove(CommandSender sender, Object[] args) {
		if(Regions.manager().getRegion((String)args[0]) == null)
			return CommandManager.CommandFinished.CUSTOM.replace("A region with that name doesn't exist.");
		
		Regions.manager().removeRegion((String)args[0]);
		MessageUtil.sendError(sender, "Region removed.");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region hearthstone", args = "<name>", help = "Toggle if a region allows hearthstones.", permission = "region.hearthstone.toggle")
	public static CommandManager.CommandFinished cmdToggleHearthstone(CommandSender sender, Object[] args) {
		Region region = Regions.manager().getRegion((String)args[0]);
		if(region == null)
			return CommandManager.CommandFinished.CUSTOM.replace("A region with that name doesn't exist.");
		
		region.allowHearthstone = !region.allowHearthstone;
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Allows Hearthstones: " + region.allowHearthstone);
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region enter", args = "<name> <enter message>", help = "Set a region enter message.", permission = "region.enter.set")
	public static CommandManager.CommandFinished cmdSetEnter(CommandSender sender, Object[] args) {
		Region region = Regions.manager().getRegion((String)args[0]);
		if(region == null)
			return CommandManager.CommandFinished.CUSTOM.replace("A region with that name doesn't exist.");
		
		region.msgEnter = (String)args[1];
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Enter: '" + region.msgEnter + "'");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region exit", args = "<name> <exit message>", help = "Set a region exit message.", permission = "region.exit.set")
	public static CommandManager.CommandFinished cmdSetSuccess(CommandSender sender, Object[] args) {
		Region region = Regions.manager().getRegion((String)args[0]);
		if(region == null)
			return CommandManager.CommandFinished.CUSTOM.replace("A region with that name doesn't exist.");
		
		region.msgExit = (String)args[1];
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Exit: '" + region.msgExit + "'");
		return CommandManager.CommandFinished.DONE;
	}

	@CommandManager.Cmd(cmd = "region failure", args = "<name> <enter failure message>", help = "Set a region failure message.", permission = "region.failure.set")
	public static CommandManager.CommandFinished cmdSetFailure(CommandSender sender, Object[] args) {
		Region region = Regions.manager().getRegion((String)args[0]);
		if(region == null)
			return CommandManager.CommandFinished.CUSTOM.replace("A region with that name doesn't exist.");
		
		region.msgFailure = (String)args[1];
		
		Regions.manager().updateRegion(region);
		MessageUtil.sendUpdate(sender, "Region updated. Failure: '" + region.msgFailure + "'");
		return CommandManager.CommandFinished.DONE;
	}
}