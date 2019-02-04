package com.legendsofvaleros.modules.quests.objective.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.modules.regions.RegionController;

public class ActionRegionAccess extends AbstractQuestAction {
	String regionId;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		RegionController.manager().setRegionAccessibility(pc, regionId, true);
		
		next.go();
	}
}