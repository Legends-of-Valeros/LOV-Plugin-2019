package com.legendsofvaleros.modules.mobs;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("mobs|lov mobs")
public class MobCommands extends BaseCommand {
	@Subcommand("clear")
	@Description("Clear the mob cache.")
	@CommandPermission("mobs.clear")
	public void cmdClear(CommandSender sender) {
		MobManager.clear();
		MessageUtil.sendUpdate(sender, "Mob cache cleared.");
	}
	
	@Subcommand("spawn")
	@Description("Create a mob spawn point at your current location with radius.")
	@CommandPermission("mobs.spawn.create")
	@Syntax("<mob id> <radius> <padding> <level min-max> [spawn count] [spawn interval] [spawn chance]")
	public void cmdCreate(Player player, String mobId, int radius, int padding, String level, @Optional Short count, @Optional Short interval, @Optional Byte chance) {
		Mob mobData = MobManager.getEntity(mobId);
		if(mobData == null) {
			MessageUtil.sendError(player, "Unknown mob with that ID.");
			return;
		}

		int[] levels;
		if(level.contains("-")) {
			levels = new int[] {
					Integer.parseInt(level.split("-")[0]),
					Integer.parseInt(level.split("-")[1])
				};
		}else{
			levels = new int[] { Integer.parseInt(level), Integer.parseInt(level) };
		}
		
		SpawnArea data = new SpawnArea(player.getLocation().getWorld().getName(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), mobId, radius, padding, levels);

		MessageUtil.sendUpdate(player, "Created spawn area with radius " + data.getRadius() + " blocks.");

		MessageUtil.sendUpdate(player, "Set spawn point " + data.getLocation() + " level to [" + data.getLevelRange()[0] + "-" + data.getLevelRange()[1] + "].");
		
		if(count != null) {
			data.spawnCount = count;
			MessageUtil.sendUpdate(player, "  Will spawn up to " + data.spawnCount + " entities.");
		}

		if(interval != null) {
			data.spawnInterval = interval;
			MessageUtil.sendUpdate(player, "  Will update about every " + data.spawnInterval + " seconds.");
			MessageUtil.sendUpdate(player, "  On Update:.");
			MessageUtil.sendUpdate(player, "    Spawn cleared when no players nearby.");
			MessageUtil.sendUpdate(player, "    If players nearby, spawn new mobs.");
		}
		
		if(chance != null) {
			data.spawnChance = chance;
			MessageUtil.sendUpdate(player, "  There is a " + data.spawnChance + "% chance it'll spawn.");
		}

		SpawnManager.addSpawn(null, data);
		SpawnManager.updateSpawn(data);
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}