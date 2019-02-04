package com.legendsofvaleros.modules.dueling.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.WorldBorderAction;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.combatengine.api.CombatEntity;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.dueling.DuelingController;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;

public class Duel extends BukkitRunnable {
	private static final ProtocolManager PM = ProtocolLibrary.getProtocolManager();

	private static final double DUEL_DIAMETER = 25;
	private static final int MAX_TIME = 30;
	private static final int DAMAGE_TIME = 10;
	
	private static final Title DRAW_TITLE = new Title("Draw!", "", 10, 40, 10);
	static {
		DRAW_TITLE.setTimingsToTicks();
		DRAW_TITLE.setTitleColor(ChatColor.GOLD);
	}
	
	Location startLocation;

	BossBar timerBar;
	long time = MAX_TIME;

	Player 			player1, 		player2;
	CombatEntity player1CE, 		player2CE;
	double[] 		p1Stats, 		p2Stats;
	
	public Duel(Player player1, Player player2) {
		this.startLocation = player1.getLocation().add(player2.getLocation().subtract(player1.getLocation()).multiply(.5D));
		
		this.timerBar = Bukkit.createBossBar("~ Dueling ~", BarColor.WHITE, BarStyle.SEGMENTED_10);
		this.timerBar.setProgress(1F);
		this.timerBar.addPlayer(player1);
		this.timerBar.addPlayer(player2);

		this.player1 = player1;
		this.player2 = player2;
		this.player1CE = CombatEngine.getEntity(player1);
		this.player2CE = CombatEngine.getEntity(player2);
		
		this.p1Stats = new double[RegeneratingStat.values().length];
		this.p2Stats = new double[RegeneratingStat.values().length];

		for(RegeneratingStat stat : RegeneratingStat.values()) {
			this.p1Stats[stat.ordinal()] = player1CE.getStats().getRegeneratingStat(stat);
			this.p2Stats[stat.ordinal()] = player2CE.getStats().getRegeneratingStat(stat);

			this.player1CE.getStats().setRegeneratingStat(stat, player1CE.getStats().getStat(stat.getMaxStat()));
			this.player2CE.getStats().setRegeneratingStat(stat, player2CE.getStats().getStat(stat.getMaxStat()));
		}
		
		showBorder();

		runTaskTimer(LegendsOfValeros.getInstance(), 20L, 20L);
	}
	
	public void onDamage(CombatEngineDamageEvent event) {
		time = Math.min(MAX_TIME, time + DAMAGE_TIME);
	}

	public void onDeath(Player died) {
		cancel();
		
		Player winner = (died != player1 ? player1 : player2);

		Title title = new Title("", winner.getName() + " won!", 10, 40, 10);
		title.setTimingsToTicks();
		title.setSubtitleColor(ChatColor.GOLD);
		TitleUtil.queueTitle(title, player1);
		TitleUtil.queueTitle(title, player2);
	}

	@Override
	public void run() {
		if(time == 0) {
			timerBar.setProgress(0F);

			TitleUtil.queueTitle(DRAW_TITLE, player1);
			TitleUtil.queueTitle(DRAW_TITLE, player2);
			
			cancel();
			return;
		}

		time--;
		
		timerBar.setProgress((float)time / MAX_TIME);
		
		if(Math.abs(startLocation.getX() - player1.getLocation().getX()) - .5D > DUEL_DIAMETER / 2
				|| Math.abs(startLocation.getZ() - player1.getLocation().getZ()) - .5D > DUEL_DIAMETER / 2) {
			MessageUtil.sendError(player1, "You left the dueling area.");
			MessageUtil.sendError(player2, "Your opponent has abandoned the duel!");
			onDeath(player1);
		
		}else if(Math.abs(startLocation.getX() - player2.getLocation().getX()) - .5D > DUEL_DIAMETER / 2
					|| Math.abs(startLocation.getZ() - player2.getLocation().getZ()) - .5D > DUEL_DIAMETER / 2) {
			MessageUtil.sendError(player2, "You left the dueling area.");
			MessageUtil.sendError(player1, "Your opponent has abandoned the duel!");
			onDeath(player2);
		}
	}

	@Override
	public synchronized void cancel() throws IllegalStateException {
		super.cancel();

		timerBar.removePlayer(player1);
		timerBar.removePlayer(player2);

		for(RegeneratingStat stat : RegeneratingStat.values()) {
			player1CE.getStats().setRegeneratingStat(stat, p1Stats[stat.ordinal()]);
			player2CE.getStats().setRegeneratingStat(stat, p2Stats[stat.ordinal()]);
		}

		hideBorder();

		DuelingController.getInstance().duels.remove(player1, player2);
	}
	
	public void showBorder() {
		PacketContainer border = PM.createPacket(PacketType.Play.Server.WORLD_BORDER);
		border.getWorldBorderActions().write(0, WorldBorderAction.INITIALIZE);
		border.getDoubles().write(0, startLocation.getX())
							.write(1, startLocation.getZ())
							.write(2, DUEL_DIAMETER) // Target side length
							.write(3, 100D); // Starting side length
		border.getLongs().write(0, 2000L); // Speed
		border.getIntegers().write(0, 29999984)
					        .write(1, 0)
					        .write(2, 0);

		try {
			PM.sendServerPacket(player1, border);
			PM.sendServerPacket(player2, border);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void hideBorder() {
		PacketContainer border = PM.createPacket(PacketType.Play.Server.WORLD_BORDER);
		border.getWorldBorderActions().write(0, WorldBorderAction.LERP_SIZE);
		border.getDoubles().write(2, 100D) // Target side length
							.write(3, DUEL_DIAMETER); // Current side length
		border.getLongs().write(0, 1000L); // Speed

		try {
			PM.sendServerPacket(player1, border);
			PM.sendServerPacket(player2, border);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		DuelingController.getInstance().getScheduler().executeInSpigotCircleLater(this::destroyBorder, 20L);
	}

	private void destroyBorder() {
		PacketContainer border = PM.createPacket(PacketType.Play.Server.WORLD_BORDER);
		border.getWorldBorderActions().write(0, WorldBorderAction.SET_SIZE);
		border.getDoubles().write(2, 30000000D);

		try {
			PM.sendServerPacket(player1, border);
			PM.sendServerPacket(player2, border);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}