package com.legendsofvaleros.modules.bank.quest;

import com.legendsofvaleros.modules.bank.Money;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionAddCurrency extends AbstractQuestAction {
	int amount;
	
	@Override
	public void play(Player player, Next next) {
		MessageUtil.sendUpdate(player, new TextBuilder("").color(ChatColor.AQUA)
				.append(String.valueOf(amount)).color(ChatColor.GREEN)
				.append(" coins!").color(ChatColor.AQUA).create());
		
		Money.add(Characters.getPlayerCharacter(player), amount);
		
		next.go();
	}
}