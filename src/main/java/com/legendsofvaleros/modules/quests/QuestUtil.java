package com.legendsofvaleros.modules.quests;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

		str = NPC.matcher(str).replaceAll(ChatColor.GREEN + "$1" + ChatColor.RESET);

		str = ITEM.matcher(str).replaceAll(ChatColor.AQUA + "$1" + ChatColor.RESET);

		/* TODO: Maybe have a system for constructing text in the editor ui. This bit of commented out code swapped
					[npc-id] to [NPC Name], however it turned out to be a tad too intensive to be run on the main thread.*/
		/*{
			Matcher npcs = NPC.matcher(str);

			if(npcs.find()) {
				StringBuffer sb = new StringBuffer();

				do {
					LOVNPC data = NPCsController.getInstance().getNPC(npcs.group(1)).get();

					String name = npcs.group(1);
					if(data != null)
						name = data.getName();

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
					Gear data = Gear.fromId(gears.group(1));

					String name = gears.group(1);
					if(data != null && data != GearController.ERROR_ITEM)
						name = data.getName();

					gears.appendReplacement(sb, ChatColor.AQUA + name + ChatColor.RESET);
				} while(gears.find());

				gears.appendTail(sb);

				str = sb.toString();
			}
		}*/

		return str;
	}
}
