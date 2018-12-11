package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActionTeleport extends AbstractQuestAction {
	String world;
	int x, y, z;
	
	@Override
	public void play(Player player, Next next) {
		player.teleport(new Location(Bukkit.getWorld(world), x, y, z));
		
		next.go();
	}
}