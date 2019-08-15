package com.legendsofvaleros.modules.questsold.action.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.regions.RegionController;

public class ActionRegionAccess extends AbstractQuestAction {
	String regionId;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		RegionController.getInstance().setRegionAccessibility(pc, regionId, true);
		
		next.go();
	}
}