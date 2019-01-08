package com.legendsofvaleros.modules.characters.core;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.codingforcookies.robert.item.Book;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLevelChangeEvent;
import com.legendsofvaleros.modules.characters.events.PlayerInformationBookEvent;
import com.legendsofvaleros.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("char|character|characters|lov char|lov character|lov characters")
public class CharacterCommands extends BaseCommand {
	@SuppressWarnings("deprecation")
	@Subcommand("level set")
	@Description("Set a character's level.")
	@CommandPermission("character.level.set")
	public void cmdSetLevel(CommandSender sender, @Optional OnlinePlayer player, int level) {
		Player p;
		if(player == null) {
			if(!(sender instanceof Player)) return;
			p = (Player)sender;
		}else
			p = player.getPlayer();

		if(!Characters.isPlayerCharacterLoaded(p)) return;

		PlayerCharacter pc = Characters.getPlayerCharacter(p);
		if(level < 0) {
			MessageUtil.sendError(sender, "Level must be greater than -1.");
		}else if(level > Characters.getInstance().getCharacterConfig().getMaxLevel()) {
			MessageUtil.sendError(sender, "That is over the max level.");
		}else{
			int oldLevel = pc.getExperience().getLevel();

			pc.getExperience().setLevel(level);

			PlayerCharacterLevelChangeEvent event = new PlayerCharacterLevelChangeEvent(pc, oldLevel, level);
			Bukkit.getPluginManager().callEvent(event);

			MessageUtil.sendUpdate(sender, "Level changed to " + level + "!");
		}
	}
	
	@SuppressWarnings("deprecation")
	@Subcommand("level up")
	@Description("Up a character's level.")
	@CommandPermission("character.level.up")
	public void cmdLevelup(CommandSender sender, @Optional OnlinePlayer player) {
		Player p;

		if(player == null) {
			if(!(sender instanceof Player)) return;
			p = (Player)sender;
		}else
			p = player.getPlayer();

		if(!Characters.isPlayerCharacterLoaded(p)) return;

		PlayerCharacter pc = Characters.getPlayerCharacter(p);
		if(pc.getExperience().getLevel() + 1 > Characters.getInstance().getCharacterConfig().getMaxLevel()) {
			MessageUtil.sendError(sender, "You are max level.");
		}else{
			pc.getExperience().addExperience(pc.getExperience().getExperienceForNextLevel(), true);
		}
	}
	
	@Default
	@Subcommand("journal")
	@Description("Open your character journal.")
	// @CommandPermission("character.journal")
	public void cmdJournal(Player player) {
		if(!Characters.isPlayerCharacterLoaded(player)) return;

		Book book = new Book("Player Information", "Acolyte");
		
		PlayerInformationBookEvent event = new PlayerInformationBookEvent(Characters.getPlayerCharacter(player));
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		for(BaseComponent[] page : event.getPages())
			book.addPage(page);
		
		book.open(player, false);
	}
}