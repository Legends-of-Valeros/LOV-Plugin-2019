package com.legendsofvaleros.modules.bank;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.cmd.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("banks|bank")
public class BankCommand extends BaseCommand {
	@Subcommand("add")
	@Description("Add \"currency\" to a player.")
	@CommandPermission("bank.edit")
	@Syntax("<player> <currency> <amount>")
	public void cmdEdit(CommandSender sender, Player player, String currency, int amount) {
		Bank.getBank(Characters.getPlayerCharacter(player)).addCurrency(currency, amount);
	}

	@Subcommand("show")
	@Description("Show bank info for player.")
	@CommandPermission("bank.show")
	@Syntax("<player>")
	public void cmdShow(CommandSender sender, Player player) {
		PlayerBank pb = Bank.getBank(Characters.getPlayerCharacter(player));
		player.sendMessage(player.getDisplayName() + "'s Bank");

		for(PlayerBank.Currency c : pb.getCurrencies())
			player.sendMessage(" " + c.getCurrencyId() + " = " + Bank.getInstance().getCurrency(c.getCurrencyId()).getDisplay(c.amount));
	}

	@Default
	public void cmdShowSelf(Player player) {
		cmdShow(player, player);
	}

	@HelpCommand
	public void cmdHelp(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}
}