package com.legendsofvaleros.modules.queue.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.legendsofvaleros.modules.queue.QueueController;
import com.legendsofvaleros.modules.queue.gui.QueueGui;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 08/02/2019
 */
@CommandAlias("queue")
public class QueueCommands extends BaseCommand {

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

    @Subcommand("leave|quit")
    @Description("Leaves the current queue you are in")
    public void onLeave(Player player) {
        QueueController.getInstance().leaveQueue(player);
    }

    @Subcommand("show")
    @Description("")
    public void onShow(Player player) {
        new QueueGui().open(player);
    }

}
