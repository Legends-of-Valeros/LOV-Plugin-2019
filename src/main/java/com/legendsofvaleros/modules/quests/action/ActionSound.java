package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class ActionSound extends AbstractQuestAction {
	String soundId;
	float volume;
	float pitch;
	
	@Override
	public void play(Player player, Next next) {
		player.playSound(player.getLocation(), soundId, SoundCategory.NEUTRAL, volume, pitch);

		next.go();
	}
}