package com.legendsofvaleros.modules.quests.action.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.modules.regions.RegionController;

public class ActionRegionDeny extends AbstractQuestAction {
	String regionId;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		RegionController.getInstance().setRegionAccessibility(pc, regionId, false);
		
		next.go();
	}
}