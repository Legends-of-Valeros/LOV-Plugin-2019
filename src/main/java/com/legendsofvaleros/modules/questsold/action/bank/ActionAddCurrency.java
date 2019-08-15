package com.legendsofvaleros.modules.questsold.action.bank;

import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;

public class ActionAddCurrency extends AbstractQuestAction {
	int amount;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		MessageUtil.sendUpdate(pc.getPlayer(), new TextBuilder("").color(ChatColor.AQUA)
				.append(String.valueOf(amount)).color(ChatColor.GREEN)
				.append(" coins!").color(ChatColor.AQUA).create());
		
		Money.add(pc, amount);
		
		next.go();
	}
}