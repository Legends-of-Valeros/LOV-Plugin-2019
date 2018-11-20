package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import org.bukkit.entity.Player;

public class ActionExperience extends AbstractAction {
	long amount;
	
	@Override
	public void play(Player player, Next next) {
		Characters.getPlayerCharacter(player).getExperience().addExperience(amount, false);
		
		next.go();
	}
}