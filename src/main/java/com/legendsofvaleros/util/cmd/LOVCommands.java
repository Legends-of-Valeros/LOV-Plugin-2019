package com.legendsofvaleros.util.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ModuleManager;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.util.*;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("lov")
public class LOVCommands extends BaseCommand {
    @Subcommand("op")
    @Description("Enable op mode.")
    @CommandPermission("lov.op")
    public void cmdOp(Player player) {
        if (Utilities.toggleOp(player))
            MessageUtil.sendUpdate(player, "Operator mode enabled.");
        else
            MessageUtil.sendUpdate(player, "Operator mode disabled.");
    }

    @Subcommand("tmp")
    @Syntax("<id>")
    private void cmdTmp(CommandSender sender, String id) {
        Runnable runnable = TemporaryCommand.get(id);
        if(runnable == null) return;
        runnable.run();
    }

    @Subcommand("tps")
    @Description("Shows the current load of all modules.")
    @CommandPermission("lov.op")
    public void cmdTps(CommandSender sender) {
        double tps = (Math.round(Lag.getTPS() * 10) + 0D) / 10D;
        long memory = Runtime.getRuntime().totalMemory();
        long memory_used = memory - Runtime.getRuntime().freeMemory();
        float memp = (memory_used + 0F) / (memory + 0F);

        ChatColor memc = ChatColor.GREEN;

        if (memp > 0.8) memc = ChatColor.YELLOW;
        if (memp > 0.87) memc = ChatColor.GOLD;
        if (memp > 0.80) memc = ChatColor.RED;
        if (memp > 0.93) memc = ChatColor.DARK_RED;

        String tpsbar = LegendsOfValeros.getInstance().createTPSBar(tps);
        String membar = ProgressBar.getBar(memp, 20, memc, ChatColor.GRAY, ChatColor.DARK_GREEN);
        String line = "------------------------------------------------";

        sender.sendMessage(ChatColor.GRAY + line);
        sender.sendMessage(ChatColor.DARK_GREEN + "Uptime: " + ChatColor.GRAY + LegendsOfValeros.getInstance().getUptime());
        sender.sendMessage(ChatColor.DARK_GREEN + "Main Server TPS: " + ChatColor.GRAY + tps + "/20.0 " + tpsbar);
        sender.sendMessage(ChatColor.DARK_GREEN + "Memory: " + ChatColor.GRAY + Lag.readableByteSize(memory_used) + "/" + Lag.readableByteSize(memory) + " " + membar);
        sender.sendMessage(ChatColor.GRAY + line);
        sender.sendMessage(ChatColor.DARK_GREEN + "Module TPS:");

        for (InternalScheduler scheduler : ModuleManager.schedulers.values()) {
            sender.sendMessage(ChatColor.DARK_GRAY + scheduler.getName() + ": " + ChatColor.GRAY + scheduler.getTPS() + "/20.0");
            sender.sendMessage("  " + LegendsOfValeros.getInstance().createTPSBar(scheduler.getTPS()));
        }

        sender.sendMessage(ChatColor.GRAY + line);
    }

    @Default
    @HelpCommand
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}