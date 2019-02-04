package com.legendsofvaleros.modules.quests.objective.gear;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;

public class ActionRemoveItem extends AbstractQuestAction {
	String itemId;
	Integer amount;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		Gear item = Gear.fromID(itemId);
		MessageUtil.sendUpdate(pc.getPlayer(), new TextBuilder("[").color(ChatColor.YELLOW)
				.append(item.getName()).color(ChatColor.GREEN)
				.append("] was removed from your inventory!").color(ChatColor.YELLOW).create());

		ItemUtil.removeItem(pc.getPlayer(), item, amount);

		next.go();
	}
}