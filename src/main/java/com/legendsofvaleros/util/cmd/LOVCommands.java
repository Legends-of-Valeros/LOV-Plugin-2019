package com.legendsofvaleros.util.cmd;

import com.legendsofvaleros.util.TemporaryCommand;
import com.legendsofvaleros.util.DebugFlags;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.TemporaryCommand;
import com.legendsofvaleros.util.Utilities;
import com.legendsofvaleros.util.cmd.CommandManager.Cmd;
import com.legendsofvaleros.util.cmd.CommandManager.CommandFinished;
import com.legendsofvaleros.util.cmd.CommandManager.CommandOnly;
import com.legendsofvaleros.util.TemporaryCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LOVCommands {
	@Cmd(cmd = "op", help = "Enable op mode.", permission = "op", only = CommandOnly.PLAYER)
	public static CommandFinished cmdOp(CommandSender sender, Object[] args) {
		if(Utilities.toggleOp((Player)sender))
			MessageUtil.sendUpdate(sender, "Operator mode enabled.");
		else
			MessageUtil.sendUpdate(sender, "Operator mode disabled.");
		return CommandFinished.DONE;
	}
	
	@Cmd(cmd = "op verbose", help = "Enable verbose mode.", permission = "op.verbose", only = CommandOnly.PLAYER)
	public static CommandFinished cmdOpVerbose(CommandSender sender, Object[] args) {
		DebugFlags flags = ensureOp((Player)sender);
		flags.verbose = !flags.verbose;
		MessageUtil.sendUpdate(sender, "Verbose: " + (flags.verbose ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
		return CommandFinished.DONE;
	}
	
	@Cmd(cmd = "op damage", help = "Enable damage debugging.", permission = "op.damage", only = CommandOnly.PLAYER)
	public static CommandFinished cmdOpDamage(CommandSender sender, Object[] args) {
		DebugFlags flags = ensureOp((Player)sender);
		flags.damage = !flags.damage;
		MessageUtil.sendUpdate(sender, "Debug Damage: " + (flags.damage ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
		return CommandFinished.DONE;
	}
	
	private static DebugFlags ensureOp(Player p) {
		if(!Utilities.isOp(p)) {
			Utilities.toggleOp(p);
			MessageUtil.sendUpdate(p, "Operator mode enabled.");
		}
		
		return DebugFlags.get(p);
	}

	@Cmd(cmd = "tmp", args = "<name>", showInHelp = false)
	public static CommandFinished cmdTmp(CommandSender sender, Object[] args) {
		Runnable runnable = TemporaryCommand.get(String.valueOf(args[0]));
		if(runnable == null)
			return CommandFinished.PERMISSION;
		runnable.run();
		return CommandFinished.DONE;
	}
	
}