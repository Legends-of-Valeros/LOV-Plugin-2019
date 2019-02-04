package com.legendsofvaleros.modules.quests.objective.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.modules.regions.Regions;

public class ActionRegionDeny extends AbstractQuestAction {
	String regionId;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		Regions.manager().setRegionAccessibility(pc, regionId, false);
		
		next.go();
	}
}