package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.combatengine.api.EntityStats;
import com.legendsofvaleros.modules.combatengine.CombatEngine;
import com.legendsofvaleros.modules.combatengine.core.StatusEffectType;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.combatengine.stat.Stat;
import com.legendsofvaleros.modules.combatengine.ui.PlayerCombatInterface;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TestUiManager implements PlayerCombatInterface {
	private Player player;
	private PlayerCharacter pc;
	private EntityStats stat;
	
	private BossBar bossBar;
	
	public TestUiManager(Player player) {
		this.player = player;
		this.pc = Characters.getPlayerCharacter(player);
		this.stat = CombatEngine.getEntity(player).getStats();
		
		this.bossBar = Bukkit.createBossBar("----------------", BarColor.PINK, BarStyle.SOLID);
		this.bossBar.setProgress(1);
		this.bossBar.addPlayer(this.player);
		
		updateBar();
	}
	
	private void updateBar() {
		new BukkitRunnable() {
			public void run() {

				String string = String.valueOf(ChatColor.RED) +
						(int) stat.getRegeneratingStat(RegeneratingStat.HEALTH) +
						"/" +
						(int) stat.getStat(Stat.MAX_HEALTH) +
						" " +
						RegeneratingStat.HEALTH.getUserFriendlyTag() +

						"           " +

						ChatColor.AQUA +
						(int) stat.getRegeneratingStat(pc.getPlayerClass().getSkillCostType()) +
						"/" +
						(int) stat.getStat(pc.getPlayerClass().getSkillCostType().getMaxStat()) +
						" " +
						pc.getPlayerClass().getSkillCostType().getUserFriendlyTag();
				bossBar.setTitle(string);
			}
		}.runTask(LegendsOfValeros.getInstance());
	}

	@Override
	public void onStatChange(Stat changed, double newValue, double oldValue) {
		switch(changed) {
			case MAX_HEALTH:
			case MAX_MANA:
			case MAX_ENERGY:
				updateBar();
			default:
				break;
		}
	}

	@Override
	public void onRegeneratingStatChange(RegeneratingStat changed, double newValue, double oldValue) {
		switch(changed) {
			case HEALTH:
				break;
			case MANA: case ENERGY:
				player.setFoodLevel((int)(stat.getRegeneratingStat(changed) / stat.getStat(changed.getMaxStat()) * 20));
				break;
		}
		
		updateBar();
	}

	@Override
	public void onStatusEffectUpdate(StatusEffectType type, long expiry) { }

	@Override
	public void onStatusEffectRemoved(StatusEffectType type, StatusEffectType.RemovalReason reason) { }

	@Override
	public void onInvalidated() {
		bossBar.removeAll();
		
		player.setFoodLevel(20);
	}
}
