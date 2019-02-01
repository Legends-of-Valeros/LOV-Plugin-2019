package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ActionTeleport extends AbstractQuestAction {
	String world;
	int x, y, z;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		pc.getPlayer().teleport(new Location(Bukkit.getWorld(world), x, y, z));
		
		next.go();
	}
}