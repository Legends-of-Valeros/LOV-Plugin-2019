package com.legendsofvaleros.util;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleManager;
import com.legendsofvaleros.module.ModuleTimings;
import com.legendsofvaleros.scheduler.InternalScheduler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

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
    @CommandPermission("lov.tps")
    public void cmdTps(CommandSender sender) {
        double mainTPS = (int)(Lag.getTPS() * 10D) / 10D;
        long memory = Runtime.getRuntime().totalMemory();
        long memory_used = memory - Runtime.getRuntime().freeMemory();
        float memp = (memory_used + 0F) / (memory + 0F);

        ChatColor memc = ChatColor.GREEN;

        if (memp > 0.8) memc = ChatColor.YELLOW;
        if (memp > 0.87) memc = ChatColor.GOLD;
        if (memp > 0.80) memc = ChatColor.RED;
        if (memp > 0.93) memc = ChatColor.DARK_RED;

        String line = "------------------------------------------------";

        sender.sendMessage(ChatColor.GRAY + line);
        sender.sendMessage(ChatColor.DARK_GREEN + "Uptime: " + ChatColor.GRAY + LegendsOfValeros.getInstance().getUptime());

        sender.sendMessage(ChatColor.DARK_GREEN + "Main Server TPS: " + ChatColor.GRAY + mainTPS + "/20.0");
        sender.sendMessage("  " + LegendsOfValeros.getInstance().createTPSBar(mainTPS));

        sender.sendMessage(ChatColor.DARK_GREEN + "Memory: " + ChatColor.GRAY + Lag.readableByteSize(memory_used) + "/" + Lag.readableByteSize(memory));
        sender.sendMessage("  " + ProgressBar.getBar(memp, 40, memc, ChatColor.GRAY, ChatColor.DARK_GREEN));

        sender.sendMessage(ChatColor.GRAY + line);

        for (InternalScheduler scheduler : ModuleManager.schedulers.values()) {
            double tps = scheduler.getAverageTPS();
            sender.sendMessage(ChatColor.DARK_GRAY + scheduler.getName() + ": " + ChatColor.GRAY + tps + "/20.0 (A: " + scheduler.getAsyncTasksFired() + " | S: " + scheduler.getSyncTasksFired() + " | +" + scheduler.getTotalBehind() + "ms)");
            sender.sendMessage("  " + LegendsOfValeros.getInstance().createTPSBar(tps));
        }

        sender.sendMessage(ChatColor.GRAY + line);
    }

    @Subcommand("tps events")
    @Description("Shows the current load of all events.")
    @CommandPermission("lov.tps.events")
    @Syntax("[module name]")
    public void cmdTpsEvents(CommandSender sender, @Optional String moduleName) {
        String line = "------------------------------------------------";

        sender.sendMessage(ChatColor.GRAY + line);

        boolean shown = false;
        for (Module module : ModuleManager.modules.values()) {
            if(moduleName != null) {
                if(!module.getName().toLowerCase().contains(moduleName.toLowerCase()))
                    continue;
            }

            ModuleTimings timings = module.getTimings();
            if(timings == null) {
                sender.sendMessage(ChatColor.RED + module.getName() + ": Error");
                continue;
            }

            double percUsed = 0;
            for(Class<? extends Event> ec : timings.getTracked())
                percUsed += timings.getAverageTPSUsage(ec);

            percUsed = (percUsed / 20D) * 100D;
            percUsed = (int)(percUsed * 100D) / 100D;

            if(moduleName == null && percUsed == 0) {
                continue;
            }

            shown = true;

            sender.sendMessage(ChatColor.DARK_GRAY + module.getName() + ": " + ChatColor.GRAY + percUsed + "% per Tick (last minute)");

            if(moduleName != null) {
                for (Class<? extends Event> ec : timings.getTracked()) {
                    percUsed = timings.getAverageTPSUsage(ec);
                    percUsed = (percUsed / 20D) * 100D;
                    percUsed = (int)(percUsed * 100D) / 100D;

                    if(percUsed > 0)
                        sender.sendMessage("  " + ChatColor.DARK_GRAY + ec.getSimpleName() + ": " + ChatColor.GRAY + percUsed + "% per Tick (" + timings.getCalls(ec) + ")");
                }
            }
        }

        if(!shown) {
            sender.sendMessage(ChatColor.GREEN + "No modules have shown significant usage in the last minute.");
        }

        sender.sendMessage(ChatColor.GRAY + line);
    }

    @Default
    @HelpCommand
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}