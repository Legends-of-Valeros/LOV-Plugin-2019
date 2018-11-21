package com.legendsofvaleros.util;

import com.codingforcookies.doris.sql.TableManager;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.Module;
import de.btobastian.javacord.entities.Channel;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MessageUtil {
	/**
	 * Had to manually expose a few variables, but this basically just prepends the required tag.
	 */
	@SuppressWarnings("unchecked")
	public static BaseComponent[] prepend(BaseComponent[] message, BaseComponent[] prepend) {
		List<BaseComponent> bb = new ArrayList<>();
		Collections.addAll(bb, prepend);
		Collections.addAll(bb, message);
		return bb.toArray(new BaseComponent[0]);
	}

	public static String resetColor(ChatColor c, String msg) {
		return msg.replace(ChatColor.RESET.toString(), c.toString());
	}

	public static void sendInfo(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.YELLOW + "[i] " + resetColor(ChatColor.YELLOW, message));
	}

	public static void sendInfo(CommandSender sender, BaseComponent[] message) {
		sender.spigot().sendMessage(prepend(message, new TextBuilder("[i] ").color(ChatColor.YELLOW).create()));
	}


	public static void sendUpdate(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.AQUA + "[!] " + resetColor(ChatColor.AQUA, message));
	}

	public static void sendUpdate(CommandSender sender, BaseComponent[] message) {
		sender.spigot().sendMessage(prepend(message, new TextBuilder("[!] ").color(ChatColor.AQUA).create()));
	}


	public static void sendError(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.RED + "[*] " + resetColor(ChatColor.RED, message));
	}

	public static void sendError(CommandSender sender, BaseComponent[] message) {
		sender.spigot().sendMessage(prepend(message, new TextBuilder("[*] ").color(ChatColor.RED).create()));
	}

	public static void sendDebug(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.GOLD + "[d] " + resetColor(ChatColor.GOLD, message));
	}

	public static void sendDebug(CommandSender sender, BaseComponent[] message) {
		sender.spigot().sendMessage(prepend(message, new TextBuilder("[d] ").color(ChatColor.GOLD).create()));
	}

	public static void sendDebugVerbose(CommandSender sender, String message) {
		if(sender == null) return;
		if(!(sender instanceof Player)) return;
		if(!DebugFlags.is((Player)sender)) return;
		if(!DebugFlags.get((Player)sender).verbose) return;
		sendDebug(sender, message);
	}

	public static void sendDebugVerbose(CommandSender sender, BaseComponent[] message) {
		if(sender == null) return;
		if(!(sender instanceof Player)) return;
		if(!DebugFlags.is((Player)sender)) return;
		if(!DebugFlags.get((Player)sender).verbose) return;
		sendDebug(sender, message);
	}

	/**
	 * @param module The module that generated the exception.
	 * @param err The error that occured.
	 * @param log If the stack trace should be logged to console.
	 */
	public static String sendException(Module module, String err, boolean log) {
		return sendException(module, null, new Exception(err), log);
	}

	/**
	 * @param module The module that generated the exception.
	 * @param sender Who to send the error to/who caused the error.
	 * @param err The error that occured.
	 * @param log If the stack trace should be logged to console.
	 */
	public static String sendException(Module module, CommandSender sender, String err, boolean log) {
		return sendException(module, sender, new Exception(err), log);
	}

	/**
	 * @param module The module that generated the exception.
	 * @param th The error object that occured.
	 * @param log If the stack trace should be logged to console.
	 */
	public static String sendException(Module module, Throwable th, boolean log) {
		return sendException(module, null, th, log);
	}

	/**
	 * @param module The module that generated the exception.
	 * @param sender Who to send the error to/who caused the error.
	 * @param th The error object that occured.
	 * @param log If the stack trace should be logged to console.
	 */
	public static String sendException(Module module, CommandSender sender, Throwable th, boolean log) {
		String trace = getStackTrace(th);
		
		BaseComponent[] bc = new TextBuilder("[X:" + module.getName() + "] " + (th.getMessage() != null ? th.getMessage() : "Something went wrong!")).color(ChatColor.DARK_RED)
				.hover(trace.replace("\t", "  ").replace("at ", ""))
				.color(ChatColor.GRAY).create();
		if(sender != null)
			sender.spigot().sendMessage(bc);
		else {
			for (Player p : Bukkit.getOnlinePlayers())
				p.spigot().sendMessage(bc);
		}

		if(log || LegendsOfValeros.getMode().doVerboseLogging())
			th.printStackTrace();

		return trace;
	}

	/**
	 * A sever exception is logged to console and sent to #logs in Discord if in a production server.
	 */
	public static void sendSevereException(Module module, Throwable th) {
		sendSevereException(module, null, th);
	}

	/**
	 * A sever exception is logged to console and sent to #logs in Discord if in a production server.
	 */
	public static void sendSevereException(Module module, CommandSender sender, Throwable th) {
		String trace = sendException(module, sender, th, true);

		th.printStackTrace();

		if(LegendsOfValeros.getMode().doLogging()) {
			ExceptionManager.add(module, sender, trace);

			if (Discord.SERVER != null) {
				// Make this channel configurable
				Channel channel = Discord.SERVER.getChannelById("358612310731915264");

				if (channel != null) {
					Utilities.getInstance().getScheduler().executeInMyCircle(() -> {
						try {
							channel.sendMessage("`[" + Discord.TAG + (module != null ? ":" + module.getName() : "") + "]` ="
									+ (sender != null ? " **__" + sender.getName() + "__ triggered an exception:**" : "")
									+ " ```" + trace + "```").get();
						} catch (InterruptedException | ExecutionException _e) {
							_e.printStackTrace();
						}
					});
				}
			}
		}
	}

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	public static void onEnable() {
		Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
			th.printStackTrace();

			String trace = getStackTrace(th);
			ExceptionManager.add(null, null, trace);
		});
	}

	public static class ExceptionManager {
		private static final String TABLE_NAME = "exceptions";

		private static final String ID_FIELD = "exception_id";
		private static final String PLUGIN_FIELD = "plugin";
		private static final String TIME = "time";
		private static final String PLAYER = "player";
		private static final String TRACE = "stack_trace";

		private static TableManager manager;

		public static void onEnable(String dbPoolId) {
			manager = new TableManager(dbPoolId, TABLE_NAME);

			manager.primary(ID_FIELD, "INT NOT NULL AUTO_INCREMENT")
				.column(PLUGIN_FIELD, "VARCHAR(32)")
				.column(TIME, "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
				.column(PLAYER, "VARCHAR(32)")
				.column(TRACE, "TEXT").create();
		}

		public static void add(Module module, CommandSender sender, String trace) {
			manager.query()
					.insert()
						.values(PLUGIN_FIELD, module == null ? "Unknown" : module.getName(),
								PLAYER, sender == null ? "Unknown" : sender.getName(),
								TRACE, trace)
					.build()
				.execute(true);
		}
	}
}