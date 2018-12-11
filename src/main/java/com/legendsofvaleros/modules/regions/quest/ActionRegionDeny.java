package com.legendsofvaleros.modules.regions.quest;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.modules.regions.Regions;
import org.bukkit.entity.Player;

public class ActionRegionDeny extends AbstractQuestAction {
	String regionId;
	
	@Override
	public void play(Player player, Next next) {
		Regions.manager().setRegionAccessibility(Characters.getPlayerCharacter(player), regionId, false);
		
		next.go();
	}
}