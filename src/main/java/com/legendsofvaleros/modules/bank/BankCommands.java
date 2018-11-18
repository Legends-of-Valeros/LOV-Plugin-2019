package com.legendsofvaleros.modules.bank;

import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommands {
	@CommandManager.Cmd(cmd = "bank edit", args = "<player> <currency> <amount>", argTypes = { CommandManager.Arg.ArgPlayer.class, CommandManager.Arg.ArgString.class, CommandManager.Arg.ArgInteger.class }, help = "Add \"currency\" to a player.", permission = "bank.edit")
	public static CommandManager.CommandFinished cmdEdit(CommandSender sender, Object[] args) {
		Bank.getBank(Characters.getPlayerCharacter((Player)args[0]))
				.addCurrency((String)args[1], (Integer)args[2]);
		return CommandManager.CommandFinished.DONE;
	}
}