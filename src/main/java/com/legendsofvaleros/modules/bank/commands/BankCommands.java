package com.legendsofvaleros.modules.bank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.bank.core.Bank;
import com.legendsofvaleros.modules.characters.core.Characters;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@CommandAlias("banks|lov banks")
public class BankCommands extends BaseCommand {
	@Subcommand("add")
	@Description("Add \"currency\" to a player.")
	@CommandPermission("bank.edit")
	@CommandCompletion("@players")
	public void cmdEdit(CommandSender sender, OnlinePlayer player, String currency, int amount) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		BankController.getBank(Characters.getPlayerCharacter(player.getPlayer())).addCurrency(currency, amount);
	}

	@Subcommand("show")
	@Description("Show bank info for player.")
	@CommandPermission("bank.show")
	public void cmdShow(CommandSender sender, OnlinePlayer player) {
		Player p = player.getPlayer();

		Bank pb = BankController.getBank(Characters.getPlayerCharacter(p));
		sender.sendMessage(p.getDisplayName() + "'s Bank");

		for(Map.Entry<String, Long> c : pb.getCurrencies().entrySet())
			sender.sendMessage(" " + c.getKey() + " = " + BankController.getInstance().getCurrency(c.getKey()).getDisplay(c.getValue()));
	}

	@Default
	public void cmdShowSelf(Player player) {
		cmdShow(player, new OnlinePlayer(player));
	}
}