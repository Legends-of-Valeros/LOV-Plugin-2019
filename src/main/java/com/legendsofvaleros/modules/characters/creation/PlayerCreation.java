package com.legendsofvaleros.modules.characters.creation;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import com.codingforcookies.robert.core.StringUtil;
import com.codingforcookies.robert.item.Book;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.config.ClassConfig;
import com.legendsofvaleros.modules.characters.config.RaceConfig;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.entityclass.EntityClass;
import com.legendsofvaleros.modules.characters.entityclass.StatModifierModel;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.characters.race.EntityRace;
import com.legendsofvaleros.modules.characters.ui.CharacterCreationListener;
import com.legendsofvaleros.modules.characters.util.ShitUtil;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.util.TextBuilder;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.*;

public class PlayerCreation implements Listener {
	private static final DecimalFormat DF = new DecimalFormat("#.00");
	
	private static Map<UUID, CreationInfo> creators = new HashMap<>();
	private static List<UUID> locked = new ArrayList<>();
	
	public static void onEnable() {
		LegendsOfValeros.getInstance().getCommandManager().registerCommand(new CreationCommand());

		Characters.getInstance().registerEvents(new PlayerCreation());
	}
	
	/**
	 * Beings creation for the specified player.
	 * @param player The player who is creating the character.
	 */
	public static void setCreating(Player player, int number, CharacterCreationListener listener) {
		creators.put(player.getUniqueId(), new CreationInfo(number, listener));
	}
	
	public static CreationInfo get(Player player) {
		if(!isCreating(player))
			return null;
		
		return creators.get(player.getUniqueId());
	}

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		creators.remove(event.getPlayer().getUniqueId());
		locked.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onCharacterLogout(PlayerCharacterLogoutEvent event) {
		creators.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onCharacterLoaded(PlayerCharacterFinishLoadingEvent event) {
		creators.remove(event.getPlayer().getUniqueId());
		locked.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onNPCClick(NPCRightClickEvent event) {
		TraitLOV lov = event.getNPC().getTrait(TraitLOV.class);
		if(lov == null) return;
		if(lov.npcId == null) return;
		if(!lov.npcId.equals(Characters.getInstance().getCharacterConfig().getCreationStartNPC())) return;
		if(Characters.isPlayerCharacterLoaded(event.getClicker())) return;
		
		event.setCancelled(true);
		
		if(!locked.contains(event.getClicker().getUniqueId())) {
			locked.add(event.getClicker().getUniqueId());
			
			ShitUtil.doShit(event.getClicker(), Characters.getInstance().getCharacterConfig().getCreationCreateText(), () -> Characters.getInstance().getUiManager().openCharacterCreation(event.getClicker()));
		}else
			Characters.getInstance().getUiManager().openCharacterCreation(event.getClicker());
	}

	/**
	 * Returns if the player is currently creating a character.
	 * 
	 * @param player The player.
	 * @return If the player is currently in character creation.
	 */
	public static boolean isCreating(Player player) {
		return creators.containsKey(player.getUniqueId());
	}

	public static void openCreation(Player player) {
		CreationInfo info = creators.get(player.getUniqueId());
		
		if(info == null) return;
		
		Book book = new Book("Character Creation", "You");
		
		TextBuilder tb =
				new TextBuilder(StringUtil.center(Book.WIDTH, player.getName()) + "\n").color(ChatColor.BLACK).underlined(true)
					.append("\nAge:       ").color(ChatColor.BLACK)
						.append("" + info.age).color(ChatColor.DARK_GRAY)
					.append("\nBirthdate: ").color(ChatColor.BLACK)
						.append(info.day + "/" + info.month + "/").color(ChatColor.DARK_GRAY)
							.append("000").color(ChatColor.DARK_GRAY).obfuscated(true)
					.append("\nRace:      ").color(ChatColor.BLACK).obfuscated(false)
						.append(info.race == null ? "[Select]" : "[" + info.race.getUserFriendlyName() + "]").color(ChatColor.DARK_PURPLE)
						.command("/creation race")
					.append("\nSchooling: ").color(ChatColor.BLACK)
						.append(info.clazz == null ? "[Select]" : "[" + info.clazz.getUserFriendlyName() + "]").color(ChatColor.DARK_PURPLE)
						.command("/creation class")
					
					.append("\n\n\n\n" + StringUtil.center(Book.WIDTH, "Certified by")).color(ChatColor.DARK_GRAY).underlined(true)
					.append("\n\nRoyal Court of ").color(ChatColor.RED).italic(true)
						.append("xxxxx").color(ChatColor.RED).obfuscated(true)
					.append("\n\n" + StringUtil.center(Book.WIDTH, "[Approve]")).color(ChatColor.DARK_GRAY);
		
		if(info.clazz != null && info.race != null)
			tb.color(ChatColor.DARK_PURPLE).command("/creation finalize");

		book.addPage(tb.create());

		book.open(player, false);
	}

	@CommandAlias("creation")
	@Private
	public static class CreationCommand extends BaseCommand {
		@Subcommand("finalize")
		@Private
		public void cmdFinalize(Player player) {
			CreationInfo info = creators.get(player.getUniqueId());
			if(info == null) return;

			info.listener.onOptionsFinalized(player, info.number, info.race, info.clazz);
		}

		@Subcommand("race")
		@Private
		public void cmdSelectRace(Player player, @Optional String selection) {
			CreationInfo info = creators.get(player.getUniqueId());
			if(info == null) return;

			if(selection == null) {
				Book book = new Book("Select Race", "You");

				StringBuilder tooltip = new StringBuilder();

				TextBuilder tb;
				for(EntityRace race : EntityRace.values()) {
					RaceConfig config = Characters.getInstance().getCharacterConfig().getRaceConfig(race);

					tb = new TextBuilder(StringUtil.center(Book.WIDTH, race.getUserFriendlyName()) + "\n").color(ChatColor.BLACK).underlined(true);

					if(config.getModifiers().size() > 0) {
						tooltip.append(ChatColor.GOLD);
						tooltip.append(ChatColor.BOLD);
						tooltip.append("Stats:");

						for(StatModifierModel mod : config.getModifiers()) {
							tooltip.append("\n ");
							tooltip.append(ChatColor.GRAY);
							tooltip.append(mod.getStat().getUserFriendlyName());
							switch(mod.getModifierType()) {
								case FLAT_EDIT:
								case FLAT_EDIT_IGNORES_MULTIPLIERS:
									tooltip.append(" + ");
									tooltip.append(DF.format(mod.getValue()));
									break;
								case MULTIPLIER:
									tooltip.append(" * ");
									tooltip.append(DF.format(mod.getValue() * 100));
									tooltip.append("%");
									break;
							}
						}
					}

					tb.append("Benefits").color(ChatColor.DARK_PURPLE).bold(true)
							.hover(tooltip.toString())
							.append("  |  ").color(ChatColor.BLACK)
							.append("Climate").color(ChatColor.DARK_PURPLE).bold(true)
							.hover(String.join("\n", config.getClimateDescription()));

					tooltip.setLength(0);

					tb.append("\n");

					for(String line : config.getDescription())
						tb.append("\n" + line).color(ChatColor.BLACK);

					tb.append("\n\n" + StringUtil.center(Book.WIDTH, "[Select this Race]")).color(ChatColor.DARK_PURPLE)
							.command("/creation race " + race.name());

					book.addPage(tb.create());
				}

				book.open(player, false);

				return;
			}

			info.race = EntityRace.valueOf(selection);

			openCreation(player);
		}

		@Subcommand("class")
		@Private
		public void cmdSelectClass(Player player, @Optional String selection) {
			CreationInfo info = creators.get(player.getUniqueId());
			if(info == null) return;

			if(selection == null) {
				Book book = new Book("Select Class", "You");

				TextBuilder tb;
				for(EntityClass clazz : EntityClass.values()) {
					if(clazz == EntityClass.PRIEST || clazz == EntityClass.ROGUE) continue;

					ClassConfig config = Characters.getInstance().getCharacterConfig().getClassConfig(clazz);

					tb = new TextBuilder(StringUtil.center(Book.WIDTH, clazz.getUserFriendlyName()) + "\n").color(ChatColor.BLACK).underlined(true);

					for(String line : config.getLongDescription())
						tb.append("\n" + line).color(ChatColor.BLACK);

					tb.append("\n\n" + StringUtil.center(Book.WIDTH, "[Select this Class]")).color(ChatColor.DARK_PURPLE)
							.command("/creation class " + clazz.name());

					book.addPage(tb.create());
				}

				book.open(player, false);

				return;
			}

			info.clazz = EntityClass.valueOf(selection);

			openCreation(player);
		}
	}
}