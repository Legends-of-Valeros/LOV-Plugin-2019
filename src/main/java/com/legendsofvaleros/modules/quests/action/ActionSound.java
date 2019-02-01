package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.SoundCategory;

public class ActionSound extends AbstractQuestAction {
	String soundId;
	float volume;
	float pitch;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		pc.getPlayer().playSound(pc.getLocation(), soundId, SoundCategory.NEUTRAL, volume, pitch);

		next.go();
	}
}