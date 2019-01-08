package com.legendsofvaleros.modules.bank;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.core.Characters;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("banks|lov banks")
public class BankCommand extends BaseCommand {
	@Subcommand("add")
	@Description("Add \"currency\" to a player.")
	@CommandPermission("bank.edit")
	@CommandCompletion("@players")
	public void cmdEdit(CommandSender sender, OnlinePlayer player, String currency, int amount) {
		if(!LegendsOfValeros.getMode().allowEditing()) return;

		Bank.getBank(Characters.getPlayerCharacter(player.getPlayer())).addCurrency(currency, amount);
	}

	@Subcommand("show")
	@Description("Show bank info for player.")
	@CommandPermission("bank.show")
	public void cmdShow(CommandSender sender, OnlinePlayer player) {
		Player p = player.getPlayer();

		PlayerBank pb = Bank.getBank(Characters.getPlayerCharacter(p));
		sender.sendMessage(p.getDisplayName() + "'s Bank");

		for(PlayerBank.Currency c : pb.getCurrencies())
			sender.sendMessage(" " + c.getCurrencyId() + " = " + Bank.getInstance().getCurrency(c.getCurrencyId()).getDisplay(c.amount));
	}

	@Default
	public void cmdShowSelf(Player player) {
		cmdShow(player, new OnlinePlayer(player));
	}
}