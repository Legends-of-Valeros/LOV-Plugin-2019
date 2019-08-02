package com.legendsofvaleros.modules.queue.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 08/02/2019
 */
@CommandAlias("queue")
public class QueueCommands {

    @Subcommand("accept")
    @Description("Accepts a queue invite on enough players")
    public void onAccept(Player player) {
        //TODO
    }

    @Subcommand("deny")
    @Description("Denies a queue invite on enough players")
    public void onDeny(Player player) {
        //TODO
    }

    @Subcommand("join")
    @Description("joins a queue by the given name")
    public void onJoin(Player player, String queueName) {
        //TODO
    }

    @Subcommand("leave")
    @Description("Leaves the current queue you are in")
    public void onLeave(Player player) {
        //TODO
    }

    @Subcommand("current")
    @Description("Requests how many people are in the current queue / ahead of you")
    public void onAmountRequest(Player player) {
        //TODO
    }


}
