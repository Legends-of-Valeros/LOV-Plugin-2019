package com.legendsofvaleros.util;

import com.codingforcookies.doris.sql.TableManager;
import com.legendsofvaleros.LegendsOfValeros;
import de.btobastian.javacord.entities.Channel;
import mkremins.fanciful.FancyMessage;
import mkremins.fanciful.MessagePart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MessageUtil {
	/**
	 * Had to manually expose a few variables, but this basically just prepends the required tag.
	 */
	@SuppressWarnings("unchecked")
	public static FancyMessage prepend(FancyMessage message, FancyMessage prepend) {
		Map<String, Object> serialized = message.serialize();
		List<MessagePart> parts = (List<MessagePart>)prepend.serialize().get("messageParts");
		((List<MessagePart>)serialized.get("messageParts")).add(0, parts.get(parts.size() - 1));
		return FancyMessage.deserialize(serialized);
	}

	public static String resetColor(ChatColor c, String msg) {
		return msg.replace(ChatColor.RESET.toString(), c.toString());
	}

	public static void sendInfo(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.YELLOW + "[i] " + resetColor(ChatColor.YELLOW, message));
	}

	public static void sendInfo(CommandSender sender, FancyMessage message) {
		prepend(message, new FancyMessage("[i] ").color(ChatColor.YELLOW)).send(sender);
	}


	public static void sendUpdate(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.AQUA + "[!] " + resetColor(ChatColor.AQUA, message));
	}

	public static void sendUpdate(CommandSender sender, FancyMessage message) {
		prepend(message, new FancyMessage("[!] ").color(ChatColor.AQUA)).send(sender);
	}


	public static void sendError(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.RED + "[*] " + resetColor(ChatColor.RED, message));
	}

	public static void sendError(CommandSender sender, FancyMessage message) {
		prepend(message, new FancyMessage("[*] ").color(ChatColor.RED)).send(sender);
	}

	public static void sendDebug(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.GOLD + "[d] " + resetColor(ChatColor.GOLD, message));
	}

	public static void sendDebug(CommandSender sender, FancyMessage message) {
		prepend(message, new FancyMessage("[d] ").color(ChatColor.GOLD)).send(sender);
	}

	public static void sendDebugVerbose(CommandSender sender, String message) {
		if(sender == null) return;
		if(!(sender instanceof Player)) return;
		if(!DebugFlags.is((Player)sender)) return;
		if(!DebugFlags.get((Player)sender).verbose) return;
		sendDebug(sender, message);
	}

	public static void sendDebugVerbose(CommandSender sender, FancyMessage message) {
		if(sender == null) return;
		if(!(sender instanceof Player)) return;
		if(!DebugFlags.is((Player)sender)) return;
		if(!DebugFlags.get((Player)sender).verbose) return;
		sendDebug(sender, message);
	}


	public static void sendException(JavaPlugin plugin, CommandSender sender, Throwable th, boolean log) {
		String trace = getStackTrace(th);
		
		FancyMessage fm = new FancyMessage("[X] " + (th.getMessage() != null ? th.getMessage() : "Something went wrong!")).color(ChatColor.DARK_RED)
				.formattedTooltip(new FancyMessage(trace.replace("\t", "  ").replace("at ", "")).color(ChatColor.GRAY));
		if(sender != null)
			fm.send(sender);
		else
			for(Player p : Bukkit.getOnlinePlayers())
				fm.send(p);
		
		if(log) {
			th.printStackTrace();
			ExceptionManager.add(plugin, sender, trace);

			if(Discord.SERVER != null) {
				// Make this channel configurable
				Channel channel = Discord.SERVER.getChannelById("358612310731915264");

				if (channel != null) {
					Bukkit.getScheduler().runTaskAsynchronously(LegendsOfValeros.getInstance(), () -> {
						try {
							channel.sendMessage(trace).get();
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

		public static void add(JavaPlugin plugin, CommandSender sender, String trace) {
			manager.query()
					.insert()
						.values(PLUGIN_FIELD, plugin == null ? "Unknown" : plugin.getName(),
								PLAYER, sender == null ? "Unknown" : sender.getName(),
								TRACE, trace)
					.build()
				.execute(true);
		}
	}
}