package com.legendsofvaleros.modules.mobs;

import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.cmd.CommandManager.Arg;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MobCommands {
	@CommandManager.Cmd(cmd = "mobs clear", help = "Clear the mob cache.", permission = "cache.clear")
	public static CommandManager.CommandFinished cmdClear(CommandSender sender, Object[] args) {
		MobManager.clear();
		MessageUtil.sendUpdate(sender, "Mob cache cleared.");
		return CommandManager.CommandFinished.DONE;
	}
	
	@CommandManager.Cmd(cmd = "mobs spawn create", args = "<radius> <padding> <instance name> <level min-level max> [spawn count] [spawn seconds] [spawn %]", argTypes = { CommandManager.Arg.ArgInteger.class, CommandManager.Arg.ArgInteger.class, CommandManager.Arg.ArgString.class, CommandManager.Arg.ArgString.class, CommandManager.Arg.ArgShort.class, CommandManager.Arg.ArgShort.class, CommandManager.Arg.ArgByte.class }, help = "Create a instance spawn.", longhelp = "Create a instance spawn at your current location with radius.", permission = "spawn.create", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdCreate(CommandSender sender, Object[] args) {
		Mob mobData = MobManager.getEntity((String)args[2]);
		if(mobData == null)
			return CommandManager.CommandFinished.CUSTOM.replace("Unknown instance ID.");

		int[] levels;
		if(String.valueOf(args[3]).contains("-")) {
			levels = new int[] {
					Integer.parseInt(String.valueOf(args[3]).split("-")[0]),
					Integer.parseInt(String.valueOf(args[3]).split("-")[1])
				};
		}else{
			levels = new int[] { Integer.parseInt(String.valueOf(args[3])), Integer.parseInt(String.valueOf(args[3])) };
		}
		
		SpawnArea data = new SpawnArea(((Player)sender).getLocation().getWorld().getName(), ((Player)sender).getLocation().getBlockX(), ((Player)sender).getLocation().getBlockY(), ((Player)sender).getLocation().getBlockZ(), (Integer)args[0], (Integer)args[1], (String)args[2], levels);

		sender.sendMessage(ChatColor.YELLOW + "Created spawn area with radius " + data.getRadius() + " blocks.");
		
		sender.sendMessage(ChatColor.YELLOW + "Set spawn point " + data.getLocation() + " level to [" + data.getLevelRange()[0] + "-" + data.getLevelRange()[1] + "].");
		
		if(args.length >= 5) {
			data.spawnCount = (Short)args[4];
			sender.sendMessage(ChatColor.YELLOW + "  Will spawn up to " + data.spawnCount + " entities.");
		}

		if(args.length >= 6) {
			data.spawnInterval = (Short)args[5];
			sender.sendMessage(ChatColor.YELLOW + "  Will update about every " + data.spawnInterval + " seconds.");
			sender.sendMessage(ChatColor.YELLOW + "  On Update:.");
			sender.sendMessage(ChatColor.YELLOW + "    Spawn cleared when no players nearby.");
			sender.sendMessage(ChatColor.YELLOW + "    If players nearby, spawn new mobs.");
		}
		
		if(args.length >= 7) {
			data.spawnChance = (Byte)args[6];
			sender.sendMessage(ChatColor.YELLOW + "  There is a " + data.spawnChance + "% chance it'll spawn.");
		}

		SpawnManager.addSpawn(null, data);
		SpawnManager.updateSpawn(data);
		
		return CommandManager.CommandFinished.DONE;
	}
}