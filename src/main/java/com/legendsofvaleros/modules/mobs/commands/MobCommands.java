package com.legendsofvaleros.modules.mobs.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.mobs.MobsController;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.modules.mobs.core.SpawnArea;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("mobs|lov mobs")
public class MobCommands extends BaseCommand {
	@Subcommand("reload")
	@Description("Reload the mob cache.")
	@CommandPermission("mobs.clear")
	public void cmdClear(CommandSender sender) {
		MobsController.getInstance().loadAll();
		MessageUtil.sendUpdate(sender, "Mob cache cleared.");
	}
	
	@Subcommand("spawn")
	@Description("Create a mob spawn point at your current location with radius.")
	@CommandPermission("mobs.spawn.create")
	@Syntax("<mob id> <radius> <padding> <level min-max> [spawn count] [spawn interval] [spawn chance]")
	public void cmdCreate(Player player, String mobId, int radius, int padding, String level, @Optional Integer count, @Optional Integer interval, @Optional Integer chance) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		IEntity entity = MobsController.getInstance().getEntity(mobId);
		if(entity == null) {
			MessageUtil.sendError(player, "Unknown entity with that ID.");
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
		
		SpawnArea data = new SpawnArea(player.getLocation(), entity, radius, padding, levels);

		MessageUtil.sendUpdate(player, "Created spawn area with radius " + data.getRadius() + " blocks.");

		MessageUtil.sendUpdate(player, "Set spawn point level to [" + data.getLevelRange()[0] + "-" + data.getLevelRange()[1] + "].");
		
		if(count != null) {
			data.setCount(count.shortValue());
			MessageUtil.sendUpdate(player, "  Will spawn up to " + data.getCount() + " entities.");
		}

		if(interval != null) {
			data.setInterval(interval);
			MessageUtil.sendUpdate(player, "  Will update about every " + data.getInterval() + " seconds.");
			MessageUtil.sendUpdate(player, "  On Update:.");
			MessageUtil.sendUpdate(player, "    Spawn cleared when no players nearby.");
			MessageUtil.sendUpdate(player, "    If players nearby, spawn new mobs.");
		}
		
		if(chance != null) {
			data.setChance(chance.byteValue());
			MessageUtil.sendUpdate(player, "  There is a " + data.getChance() + "% chance it'll spawn.");
		}

		MobsController.getInstance().addSpawn(data);
		MobsController.getInstance().updateSpawn(data);
	}

	@Default
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}