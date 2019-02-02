package com.legendsofvaleros.modules.quests;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.npcs.NPCData;
import com.legendsofvaleros.modules.npcs.NPCs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestUtil {
	private static final Pattern TITLE = Pattern.compile("<(.+?)>");
	private static final Pattern NPC = Pattern.compile("\\[(.+?)\\]");
	private static final Pattern ITEM = Pattern.compile("\\{(.+?)\\}");

	public static String moustache(Player player, String str) {
		return moustache(Characters.getPlayerCharacter(player), str);
	}

	public static String moustache(PlayerCharacter pc, String str) {
		str = str.replaceAll("\\{\\{name\\}\\}", ChatColor.GREEN + pc.getPlayer().getName() + ChatColor.RESET);
		str = str.replaceAll("\\{\\{level\\}\\}", ChatColor.GREEN + String.valueOf(pc.getExperience().getLevel()) + ChatColor.RESET);
		str = str.replaceAll("\\{\\{class\\}\\}", ChatColor.GREEN + pc.getPlayerClass().getUserFriendlyName() + ChatColor.RESET);
		str = str.replaceAll("\\{\\{race\\}\\}", ChatColor.GREEN + pc.getPlayerRace().getUserFriendlyName() + ChatColor.RESET);

		str = TITLE.matcher(str).replaceAll(ChatColor.YELLOW + "$1" + ChatColor.RESET);

		{
			Matcher npcs = NPC.matcher(str);

			if(npcs.find()) {
				StringBuffer sb = new StringBuffer();

				do {
					NPCData data = NPCs.getNPC(npcs.group(1));

					String name = npcs.group(1);
					if(data != null)
						name = data.name;

					npcs.appendReplacement(sb, ChatColor.GREEN + name + ChatColor.RESET);
				} while(npcs.find());

				npcs.appendTail(sb);

				str = sb.toString();
			}
		}

		{
			Matcher gears = ITEM.matcher(str);

			if(gears.find()) {
				StringBuffer sb = new StringBuffer();

				do {
					Gear data = Gear.fromID(gears.group(1));

					String name = gears.group(1);
					if(data != null)
						name = data.getName();

					gears.appendReplacement(sb, ChatColor.AQUA + name + ChatColor.RESET);
				} while(gears.find());

				gears.appendTail(sb);

				str = sb.toString();
			}
		}

		return str;
	}
}
