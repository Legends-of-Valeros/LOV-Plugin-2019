package com.legendsofvaleros.modules.characters.core;

import com.codingforcookies.robert.item.Book;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLevelUpEvent;
import com.legendsofvaleros.modules.characters.events.PlayerInformationBookEvent;
import com.legendsofvaleros.util.cmd.CommandManager;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.util.MessageUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CharacterCommands {
	@SuppressWarnings("deprecation")
	@CommandManager.Cmd(cmd = "setlevel", args = "<level>", argTypes = { CommandManager.Arg.ArgInteger.class }, help = "Set your level.", permission = "character.level.set", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdSetLevel(CommandSender sender, Object[] args) {
		if(!Characters.isPlayerCharacterLoaded((Player)sender)) return CommandManager.CommandFinished.CUSTOM.replace("You have not selected a character.");
		
		int level = (Integer)args[0];
		
		PlayerCharacter pc = Characters.getPlayerCharacter((Player)sender);
		if(level < 0) {
			MessageUtil.sendError(sender, "Level must be greater than -1.");
		}else if(level > Characters.inst().getCharacterConfig().getMaxLevel()) {
			MessageUtil.sendError(sender, "That is over the max level.");
		}else{
			pc.getExperience().setLevel(level);
			
			PlayerCharacterLevelUpEvent event = new PlayerCharacterLevelUpEvent(pc, pc.getExperience().getLevel());
			Bukkit.getPluginManager().callEvent(event);
			
			MessageUtil.sendUpdate(sender, "Level changed to " + level + "!");
		}
		
		return CommandManager.CommandFinished.DONE;
	}
	
	@SuppressWarnings("deprecation")
	@CommandManager.Cmd(cmd = "levelup", help = "Level yourself up.", permission = "character.level.up", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdLevelup(CommandSender sender, Object[] args) {
		if(!Characters.isPlayerCharacterLoaded((Player)sender)) return CommandManager.CommandFinished.CUSTOM.replace("You have not selected a character.");
		
		PlayerCharacter pc = Characters.getPlayerCharacter((Player)sender);
		if(pc.getExperience().getLevel() + 1 > Characters.inst().getCharacterConfig().getMaxLevel()) {
			MessageUtil.sendError(sender, "You are max level.");
		}else{
			pc.getExperience().setLevel(pc.getExperience().getLevel() + 1);
			
			PlayerCharacterLevelUpEvent event = new PlayerCharacterLevelUpEvent(pc, pc.getExperience().getLevel());
			Bukkit.getPluginManager().callEvent(event);
			
			MessageUtil.sendUpdate(sender, "Leveled up!");
		}
		
		return CommandManager.CommandFinished.DONE;
	}
	
	@CommandManager.Cmd(cmd = "journal", only = CommandManager.CommandOnly.PLAYER)
	public static CommandManager.CommandFinished cmdJournal(CommandSender sender, Object[] args) {
		Player p = (Player)sender;
		if(!Characters.isPlayerCharacterLoaded(p)) return CommandManager.CommandFinished.DONE;

		Book book = new Book("Player Information", "Acolyte");
		
		PlayerInformationBookEvent event = new PlayerInformationBookEvent(Characters.getPlayerCharacter(p));
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		for(FancyMessage page : event.getPages())
			book.addPage(page);
		
		book.open(p, false);

		return CommandManager.CommandFinished.DONE;
	}
}