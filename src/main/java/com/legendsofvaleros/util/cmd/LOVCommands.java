package com.legendsofvaleros.util.cmd;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ModuleManager;
import com.legendsofvaleros.scheduler.InternalScheduler;
import com.legendsofvaleros.util.*;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LOVCommands {
    @Cmd(cmd = "op", help = "Enable op mode.", permission = "op", only = CommandOnly.PLAYER)
    public static CommandFinished cmdOp(CommandSender sender, Object[] args) {
        if (Utilities.toggleOp((Player) sender))
            MessageUtil.sendUpdate(sender, "Operator mode enabled.");
        else
            MessageUtil.sendUpdate(sender, "Operator mode disabled.");
        return CommandFinished.DONE;
    }

    @Cmd(cmd = "op verbose", help = "Enable verbose mode.", permission = "op.verbose", only = CommandOnly.PLAYER)
    public static CommandFinished cmdOpVerbose(CommandSender sender, Object[] args) {
        DebugFlags flags = ensureOp((Player) sender);
        flags.verbose = !flags.verbose;
        MessageUtil.sendUpdate(sender, "Verbose: " + (flags.verbose ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        return CommandFinished.DONE;
    }

    @Cmd(cmd = "op damage", help = "Enable damage debugging.", permission = "op.damage", only = CommandOnly.PLAYER)
    public static CommandFinished cmdOpDamage(CommandSender sender, Object[] args) {
        DebugFlags flags = ensureOp((Player) sender);
        flags.damage = !flags.damage;
        MessageUtil.sendUpdate(sender, "Debug Damage: " + (flags.damage ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        return CommandFinished.DONE;
    }

    private static DebugFlags ensureOp(Player p) {
        if (!Utilities.isOp(p)) {
            Utilities.toggleOp(p);
            MessageUtil.sendUpdate(p, "Operator mode enabled.");
        }

        return DebugFlags.get(p);
    }

    @Cmd(cmd = "tmp", args = "<name>", showInHelp = false)
    public static CommandFinished cmdTmp(CommandSender sender, Object[] args) {
        Runnable runnable = TemporaryCommand.get(String.valueOf(args[0]));
        if (runnable == null)
            return CommandFinished.PERMISSION;
        runnable.run();
        return CommandFinished.DONE;
    }

    @Cmd(cmd = "tps", help = "Shows the current load of all modules")
    public static CommandFinished cmdTps(CommandSender sender, Object[] args) {
        Player p = (Player) sender;
        double tps = (Math.round(Lag.getTPS() * 10) + 0D) / 10D;
        long memory = Runtime.getRuntime().totalMemory();
        long memory_used = memory - Runtime.getRuntime().freeMemory();
        float memp = (memory_used + 0F) / (memory + 0F);
        org.bukkit.ChatColor memc = org.bukkit.ChatColor.GREEN;
        if (memp > 0.8) memc = org.bukkit.ChatColor.YELLOW;
        if (memp > 0.87) memc = org.bukkit.ChatColor.GOLD;
        if (memp > 0.80) memc = org.bukkit.ChatColor.RED;
        if (memp > 0.93) memc = org.bukkit.ChatColor.DARK_RED;
        String tpsbar = LegendsOfValeros.getInstance().createTPSBar(tps);
        String membar = ProgressBar.getBar(memp, 20, memc, org.bukkit.ChatColor.GRAY, org.bukkit.ChatColor.DARK_GREEN);
        String line = "------------------------------------------------";
        p.sendMessage(org.bukkit.ChatColor.GRAY + line);
        p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "Uptime: " + org.bukkit.ChatColor.GRAY + LegendsOfValeros.getInstance().getUptime());
        p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "Main Server TPS: " + org.bukkit.ChatColor.GRAY + tps + "/20.0 " + tpsbar);
        p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "Memory: " + org.bukkit.ChatColor.GRAY + Lag.readableByteSize(memory_used) + "/" + Lag.readableByteSize(memory) + " " + membar);
        p.sendMessage(org.bukkit.ChatColor.GRAY + line);
        p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "Module TPS:");
        for (InternalScheduler scheduler : ModuleManager.schedulers.values()) {
            p.sendMessage(org.bukkit.ChatColor.DARK_GRAY + scheduler.getName() + ": " + org.bukkit.ChatColor.GRAY + scheduler.getTPS() + "/20.0 " + LegendsOfValeros.getInstance().createTPSBar(scheduler.getTPS()));
        }
        p.sendMessage(org.bukkit.ChatColor.GRAY + line);
        return CommandFinished.DONE;
    }
}