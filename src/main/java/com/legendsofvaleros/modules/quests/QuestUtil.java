package com.legendsofvaleros.modules.quests;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class QuestUtil {
	public static String moustache(Player player, String str) {
		PlayerCharacter pc = Characters.getPlayerCharacter(player);
		
		str = str.replaceAll("\\{\\{name\\}\\}", ChatColor.AQUA + player.getName() + ChatColor.RESET);
		str = str.replaceAll("\\{\\{level\\}\\}", ChatColor.AQUA + String.valueOf(pc.getExperience().getLevel()) + ChatColor.RESET);
		str = str.replaceAll("\\{\\{class\\}\\}", ChatColor.AQUA + pc.getPlayerClass().getUserFriendlyName() + ChatColor.RESET);
		str = str.replaceAll("\\{\\{race\\}\\}", ChatColor.AQUA + pc.getPlayerRace().getUserFriendlyName() + ChatColor.RESET);

		str = str.replaceAll("<(.+?)>", ChatColor.YELLOW + "$1" + ChatColor.RESET);
		str = str.replaceAll("\\[(.+?)\\]", ChatColor.GREEN + "$1" + ChatColor.RESET);
		str = str.replaceAll("\\{(.+?)\\}", ChatColor.AQUA + "$1" + ChatColor.RESET);
		
		return str;
	}
}
