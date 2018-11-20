package com.legendsofvaleros.modules.gear.quest;

import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.gear.util.ItemUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionRemoveItem extends AbstractAction {
	String itemId;
	Integer amount;
	
	@Override
	public void play(Player player, Next next) {
		GearItem item = GearItem.fromID(itemId);
		MessageUtil.sendUpdate(player, new TextBuilder("[").color(ChatColor.YELLOW)
				.append(item.getName()).color(ChatColor.GREEN)
				.append("] was removed from your inventory!").color(ChatColor.YELLOW).create());

		ItemUtil.removeItem(player, item, amount);

		next.go();
	}
}