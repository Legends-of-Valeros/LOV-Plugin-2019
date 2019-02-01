package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TextBuilder;
import com.legendsofvaleros.util.commands.TemporaryCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

// XXX: This whole action is garbage.
public class ActionConversation extends AbstractQuestAction {
	String option_1, option_2, option_3, option_4, option_5;
	int action_1, action_2, action_3, action_4, action_5;
	
	@Override
	public void play(PlayerCharacter pc, Next next) {
		MessageUtil.sendInfo(pc.getPlayer(), "Select one to continue:");
		
		List<String> tmps = new ArrayList<>();

		if(option_1 != null && option_1.length() > 0)
			register(tmps, pc.getPlayer(), option_1, () -> next.go(action_1));
		
		if(option_2 != null && option_2.length() > 0)
			register(tmps, pc.getPlayer(), option_2, () -> next.go(action_2));
		
		if(option_3 != null && option_3.length() > 0)
			register(tmps, pc.getPlayer(), option_3, () -> next.go(action_3));
		
		if(option_4 != null && option_5.length() > 0)
			register(tmps, pc.getPlayer(), option_4, () -> next.go(action_4));
		
		if(option_5 != null && option_5.length() > 0)
			register(tmps, pc.getPlayer(), option_5, () -> next.go(action_5));
	}
	
	private void register(List<String> tmps, Player player, String text, Runnable run) {
		String tmp = TemporaryCommand.register(() -> {
			run.run();
			
			unregister(tmps);
		});
		
		tmps.add(tmp);
		
		player.spigot().sendMessage(new TextBuilder(" [").color(ChatColor.YELLOW)
				.append(text).command("/tmp " + tmp).color(ChatColor.WHITE)
				.append("]").color(ChatColor.YELLOW).create());

	}
	
	private void unregister(List<String> tmps) {
		for(String tmp : tmps)
			TemporaryCommand.unregister(tmp);
	}
}