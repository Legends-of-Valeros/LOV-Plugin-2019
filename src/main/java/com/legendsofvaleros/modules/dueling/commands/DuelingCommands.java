package com.legendsofvaleros.modules.dueling.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 07/26/2019
 */
@CommandAlias("dueling|duels")
public class DuelingCommands {

    @Subcommand("invite")
    @Description("Invites a player to a duel")
    public void onInvite(Player p, OnlinePlayer onlinePlayer) {
        //TODO ADD ACCEPT COMMAND
    }

    @Subcommand("accpet")
    @Description("Accepts a pending duel invite")
    public void onAccept(Player p) {
        //TODO ADD ACCEPT COMMAND
    }

    @Subcommand("deny")
    @Description("Denies a pending duel invite")
    public void onDeny(Player p) {
        //TODO ADD DENY COMMAND
    }
}
