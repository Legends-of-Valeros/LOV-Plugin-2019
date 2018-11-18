package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;

public class ActionTeleport extends AbstractAction {
	String world;
	int x, y, z;
	
	@Override
	public void play(Player player, Next next) {
		player.teleport(new Location(Bukkit.getWorld(world), x, y, z));
		
		next.go();
	}
}