package com.legendsofvaleros.util.title;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class TitleUtil {
	private static HashMap<Player, TitleQueue> titles = new HashMap<>();

	public static void queueTitle(Title title, Player player) {
		if(!titles.containsKey(player))
			titles.put(player, new TitleQueue(player));
		titles.get(player).queue(title);
	}
}

class TitleQueue {
	public Player player;
	public ArrayList<Title> titles = new ArrayList<>();
	
	public TitleQueue(Player player) {
		this.player = player;
	}

	public void queue(final Title title) {
		titles.add(title);
		
		if(titles.size() == 1)
			doQueue();
	}
	
	private void doQueue() {
		final Title title = titles.get(0);
		title.send(player);
		
		final int titleTime = (title.getFadeInTime() + title.getStayTime() + title.getFadeOutTime()) * (title.isTicks() ? 1 : 20);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				titles.remove(0);

				if(titles.size() != 0)
					doQueue();
			}
		}.runTaskLater(LegendsOfValeros.getInstance(), titleTime);
	}
}