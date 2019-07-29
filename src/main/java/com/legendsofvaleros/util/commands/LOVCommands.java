package com.legendsofvaleros.util.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleEventTimings;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.util.Lag;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

@CommandAlias("performance|pf|lov performance|lov pf")
public class LOVCommands extends BaseCommand {
    @Subcommand("schedulers|sched")
    @Description("Shows the current load of all schedulers.")
    @CommandPermission("performance.tps")
    public void cmdTps(CommandSender sender) {
        double[] mainTPS = Bukkit.getTPS();
        long memory = Runtime.getRuntime().totalMemory();
        long memoryUsed = memory - Runtime.getRuntime().freeMemory();
        float memp = (memoryUsed + 0F) / (memory + 0F);

        ChatColor memc = ChatColor.GREEN;

        if (memp > 0.8) memc = ChatColor.YELLOW;
        if (memp > 0.87) memc = ChatColor.GOLD;
        if (memp > 0.80) memc = ChatColor.RED;
        if (memp > 0.93) memc = ChatColor.DARK_RED;

        String line = "------------------------------------------------";

        sender.sendMessage(ChatColor.GRAY + line);
        sender.sendMessage(ChatColor.DARK_GREEN + "Uptime: " + ChatColor.GRAY + Utilities.getUptime());

        sender.sendMessage(ChatColor.DARK_GREEN + "Main Server TPS (1m): " + ChatColor.GRAY + mainTPS[0] + "/20.0");
        sender.sendMessage("  " + Lag.createTPSBar(mainTPS[0]));
        sender.sendMessage(ChatColor.DARK_GREEN + "Main Server TPS (5m): " + ChatColor.GRAY + mainTPS[1] + "/20.0");
        sender.sendMessage("  " + Lag.createTPSBar(mainTPS[1]));
        sender.sendMessage(ChatColor.DARK_GREEN + "Main Server TPS (15m): " + ChatColor.GRAY + mainTPS[2] + "/20.0");
        sender.sendMessage("  " + Lag.createTPSBar(mainTPS[2]));

        sender.sendMessage(ChatColor.DARK_GREEN + "Memory: " + ChatColor.GRAY + Lag.readableByteSize(memoryUsed) + "/" + Lag.readableByteSize(memory));
        sender.sendMessage("  " + Lag.getBar(memp, 40, memc, ChatColor.GRAY, ChatColor.DARK_GREEN));

        sender.sendMessage(ChatColor.GRAY + line);

        for (InternalScheduler scheduler : Modules.getSchedulers()) {
            double tps = scheduler.getAverageTPS();
            sender.sendMessage((scheduler.isAlive() ? ChatColor.DARK_GRAY : ChatColor.RED) + scheduler.getName() + ": " + ChatColor.GRAY + tps + "/20.0 (A: " + scheduler.getAsyncTasksFired() + " | S: " + scheduler.getSyncTasksFired() + " | +" + scheduler.getTotalBehind() + "ms)");
            sender.sendMessage("  " + Lag.createTPSBar(tps));
        }

        sender.sendMessage(ChatColor.GRAY + line);
    }

    @Subcommand("events")
    @Description("Shows the current load of all event handlers.")
    @CommandPermission("performance.events")
    public void cmdTpsEvents(CommandSender sender, @Optional String moduleName) {
        String line = "------------------------------------------------";

        sender.sendMessage(ChatColor.GRAY + line);

        boolean shown = false;
        for (Module module : Modules.getLoadedModules()) {
            if (moduleName != null) {
                if (!module.getName().toLowerCase().contains(moduleName.toLowerCase())) {
                    continue;
                }
            }

            ModuleEventTimings timings = module.getTimings();
            if (timings == null) {
                sender.sendMessage(ChatColor.RED + module.getName() + ": Error");
                continue;
            }

            double percUsed = 0;
            for (Class<? extends Event> ec : timings.getTracked()) {
                percUsed += timings.getAverageTPSUsage(ec);
            }

            percUsed = (percUsed / 20D) * 100D;
            percUsed = (int) (percUsed * 100D) / 100D;

            if (moduleName == null && percUsed == 0) {
                continue;
            }

            shown = true;
            sender.sendMessage(ChatColor.DARK_GRAY + module.getName() + ": " + ChatColor.GRAY + percUsed + "% per Tick (last minute)");

            if (moduleName != null) {
                for (Class<? extends Event> ec : timings.getTracked()) {
                    percUsed = timings.getAverageTPSUsage(ec);
                    percUsed = (percUsed / 20D) * 100D;
                    percUsed = (int) (percUsed * 100D) / 100D;

                    if (percUsed > 0) {
                        sender.sendMessage("  " + ChatColor.DARK_GRAY + ec.getSimpleName() + ": " + ChatColor.GRAY + percUsed + "% per Tick (" + timings.getCalls(ec) + ")");
                    }
                }
            }
        }

        if (!shown) {
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