package com.legendsofvaleros.modules.characters.testing;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.*;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineRegenEvent;
import com.legendsofvaleros.modules.combatengine.stat.RegeneratingStat;
import com.legendsofvaleros.modules.playermenu.InventoryManager;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.modules.playermenu.options.PlayerOptionsOpenEvent;
import com.legendsofvaleros.util.item.Model;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.ActionBar;
import com.legendsofvaleros.util.MessageUtil;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TestListener implements Listener {
	public TestListener() {
		Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
		Bukkit.getPluginManager().registerEvents(new PlayerBookListener(), LegendsOfValeros.getInstance());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMenuOpen(PlayerMenuOpenEvent event) {
		if(!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}

		if(Characters.inst().isInCombat(event.getPlayer())) {
			MessageUtil.sendError(event.getPlayer(), "You cannot do that while in combat!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCharacterOptionsOpen(PlayerOptionsOpenEvent event) {
		if(!Characters.isPlayerCharacterLoaded(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		event.addSlot(Model.stack("menu-characters-button").setName("Character Selection").create(), (gui, p, event1) -> Characters.openCharacterSelection(p));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFillInventory(PlayerCharacterInventoryFillEvent e) {
		InventoryManager.fillInventory(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerCharacterStartLoading(PlayerCharacterStartLoadingEvent event) {
		if(event.getPlayer().hasPotionEffect(PotionEffectType.BLINDNESS))
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1), true);
		
		event.getPlayer().setLevel(0);
		event.getPlayer().setExp(0F);

		/*event.registerLockingTask(new Runnable() {
				@Override
				public void run() {
					try {

						Thread.sleep(fSleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});*/
	}

	@EventHandler
	public void onPlayerCharacterFinishLoading(final PlayerCharacterFinishLoadingEvent event) {
		event.getPlayer().setLevel(event.getPlayerCharacter().getExperience().getLevel());
		event.getPlayer().setExp((float)event.getPlayerCharacter().getExperience().getPercentageTowardsNextLevel());
	}

	@EventHandler
	public void onPlayerCharacterLogout(final PlayerCharacterLogoutEvent event) {
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1), true);
	}
	
	@EventHandler
	public void onRegenHealth(CombatEngineRegenEvent event) {
		if(!event.getCombatEntity().isPlayer()) return;
		if(event.getRegenerating() != RegeneratingStat.HEALTH) return;
		
		if(Characters.inst().isInCombat((Player)event.getCombatEntity().getLivingEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onNPCLeftClick(NPCLeftClickEvent event) {
		if(!event.getNPC().hasTrait(TraitLOV.class)) return;
		if(!Characters.inst().isInCombat(event.getClicker())) return;
		
		MessageUtil.sendError(event.getClicker(), "You cannot do that while in combat!");
		
		event.setCancelled(true);
	}

	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent event) {
		if(!event.getNPC().hasTrait(TraitLOV.class)) return;
		if(!Characters.inst().isInCombat(event.getClicker())) return;
		
		MessageUtil.sendError(event.getClicker(), "You cannot do that while in combat!");
		
		event.setCancelled(true);
	}

	@EventHandler
	public void onFoodLoss(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onToggleSprint(PlayerToggleSprintEvent event) {
		event.setCancelled(false);
	}

	@EventHandler
	public void onValerosExperienceChange(PlayerCharacterExperienceChangeEvent event) {
		Player player = event.getPlayer();
		ActionBar.set(player, ChatColor.AQUA + "+" + event.getChange() + "xp");
		player.setExp((float)event.getPlayerCharacter().getExperience().getPercentageTowardsNextLevel());
	}

	@EventHandler
	public void onValerosLevelUp(PlayerCharacterLevelUpEvent event) {
		Player player = event.getPlayer();
		ActionBar.set(player, ChatColor.YELLOW + "You have leveled up!");

		player.setLevel(event.getNewLevel());
		player.setExp(0F);
		
		player.playSound(player.getLocation(), "misc.levelup", 1F, 1F);
	}

	/*
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Player player = event.getPlayer();
			PlayerCharacter pc = Characters.getPlayerCharacter(player);
			if (pc != null) {
				if (event.getClickedBlock().getType() == Material.GRASS) {
					AbilityStats stats = Characters.getPlayerCharacter(player).getAbilityStats();

					for (AbilityStat abilityStat : AbilityStat.values())
						Bukkit.broadcastMessage(ChatColor.GREEN + "" + abilityStat + ": " + stats.getAbilityStat(abilityStat));
				} else if (event.getClickedBlock().getType() == Material.LEAVES) {
					Bukkit.broadcastMessage(ChatColor.GREEN + "Refreshing listener...");
					final ListenableFuture<Experience> fut = pc.getExperience().refresh();
					fut.addListener(new Runnable() {
						@Override
						public void run() {
							Bukkit.getScheduler().runTask(Characters.getPlugin(), new Runnable() {
								@Override
								public void run() {
									try {
										Experience exp = fut.get();
										Bukkit.broadcastMessage(ChatColor.GREEN + "Experience was refreshed. Level: "
												+ exp.getLevel() + ", Exp: " + exp.getExperienceTowardsNextLevel()
												+ ", percentage: " + exp.getPercentageTowardsNextLevel());
									} catch (Exception e) {
										Bukkit.broadcastMessage(ChatColor.RED
												+ "There was a problem refreshing listener");
										e.printStackTrace();
									}
								}
							});
						}
					}, Utilities.asyncExecutor());

				} else if (event.getClickedBlock().getType() == Material.DIRT) {
					player.sendMessage("");
					player.sendMessage(ChatColor.GREEN + "Your skill-effects:");
					for (SkillEffect<? extends Object> effect : Characters.inst()
							.getSkillEffectManager().getActiveEffects(player)) {
						player.sendMessage(ChatColor.GREEN + " " + effect.getUserFriendlyName(player));
						for (String detail : effect.getUserFriendlyDetails(player)) {
							player.sendMessage(ChatColor.GREEN + "  " + detail);
						}
						player.sendMessage(ChatColor.GREEN
								+ "  Remaining duration: "
								+ TimeStrings.getTimeFromMilliseconds(effect.getEntityInstance(player)
										.getRemainingDurationMillis(), 4, false));
					}
					player.sendMessage("");


				} else if (event.getClickedBlock().getType() == Material.SAND) {
					int level = ThreadLocalRandom.current().nextInt(5);
					player.sendMessage(ChatColor.GREEN + "Casting max-health buff "
							+ RomanNumeral.convertToRoman(level) + " on you!");
					boolean succeeded =
							Characters.inst().getSkillEffectManager().getSkillEffect("MaxHealthBuff")
							.apply(player, player, level);
					if (succeeded) {
						player.sendMessage(ChatColor.GREEN + "Succeeded.");
					} else {
						player.sendMessage(ChatColor.RED + "Failed to override the previous effect");
					}

					level = ThreadLocalRandom.current().nextInt(1) + 1;
			          player.sendMessage(ChatColor.GREEN + "Casting percentage poison "
			              + RomanNumeral.convertToRoman(level) + " on you!");
			          succeeded =
			              Characters.inst().getSkillEffectManager().getSkillEffect("PercentagePoison")
			                  .apply(player, player, level);
			          if (succeeded) {
			            player.sendMessage(ChatColor.GREEN + "Succeeded.");
			          } else {
			            player.sendMessage(ChatColor.RED + "Failed to override the previous effect");
			          }

					CombatEntity ce = CombatEngine.inst().getCombatEntity(player);
					ce.getStatusEffects().addStatusEffect(StatusEffectType.BLINDNESS, 400);
				}
			}
		}
	}

	@EventHandler
	public void onCombatEngineDamage(CombatEngineDamageEvent event) {
		SkillEffect<? extends Object> effect =
				Characters.inst().getSkillEffectManager().getSkillEffect("PercentagePoison");
		if (event.getAttacker() != null && event.getAttacker().isPlayer()
				&& !event.getAttacker().equals(event.getDamaged())
				&& !effect.isAffected(event.getDamaged().getLivingEntity())) {
			int level = ThreadLocalRandom.current().nextInt(1) + 1;
			Player player = (Player) event.getAttacker().getLivingEntity();
			player.sendMessage(ChatColor.GREEN + "Casting percentage poison "
					+ RomanNumeral.convertToRoman(level) + " on your target!");
			boolean succeeded = effect.apply(event.getDamaged().getLivingEntity(), player, level);
			if (succeeded) {
				player.sendMessage(ChatColor.GREEN + "Succeeded.");
			} else {
				player.sendMessage(ChatColor.RED + "Failed to override the previous effect");
			}
		}
	}

	private void printCooldown(String prefix, Cooldown cd) {
		if (cd != null) {
			System.out.println(prefix + " key: " + cd.getKey() + ", type: " + cd.getCooldownType().name()
					+ ", remaining milliseconds " + cd.getRemainingDurationMillis());
		} else {
			System.out.println(prefix + " cd = null");
		}
	}*/

}
