package com.legendsofvaleros.modules.quests.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;

public class ActionText extends AbstractQuestAction {
	String format;
	String text;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		switch (format) {
			case "TEXT":
				pc.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(pc, text)));
				break;
			case "INFO":
				MessageUtil.sendInfo(pc.getPlayer(), ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(pc, text)));
				break;
			case "UPDATE":
				MessageUtil.sendUpdate(pc.getPlayer(), ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(pc, text)));
				break;
			case "ERROR":
				MessageUtil.sendError(pc.getPlayer(), ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(pc, text)));
				break;
		}
		
		next.go();
	}
}