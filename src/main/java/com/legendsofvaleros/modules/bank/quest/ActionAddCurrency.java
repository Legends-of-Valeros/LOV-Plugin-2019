package com.legendsofvaleros.modules.bank.quest;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionAddCurrency extends AbstractAction {
	int amount;
	
	@Override
	public void play(Player player, Next next) {
		MessageUtil.sendUpdate(player, new FancyMessage("").color(ChatColor.AQUA)
				.then(String.valueOf(amount)).color(ChatColor.GREEN)
				.then(" coins!").color(ChatColor.AQUA));
		
		Money.add(Characters.getPlayerCharacter(player), amount);
		
		next.go();
	}
}