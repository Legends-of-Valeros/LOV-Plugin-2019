package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionText extends AbstractAction {
	String format;
	String text;
	
	@Override
	public void play(Player player, Next next) {
		switch (format) {
			case "TEXT":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(player, text)));
				break;
			case "INFO":
				MessageUtil.sendInfo(player, ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(player, text)));
				break;
			case "UPDATE":
				MessageUtil.sendUpdate(player, ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(player, text)));
				break;
			case "ERROR":
				MessageUtil.sendError(player, ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(player, text)));
				break;
		}
		
		next.go();
	}
}