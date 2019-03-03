package com.legendsofvaleros.util;

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
    @SuppressWarnings("unchecked")
    public static BaseComponent[] combine(BaseComponent[] bc1, BaseComponent[] bc2) {
        List<BaseComponent> bb = new ArrayList<>();
        Collections.addAll(bb, bc1);
        Collections.addAll(bb, bc2);
        return bb.toArray(new BaseComponent[0]);
    }

    @SuppressWarnings("unchecked")
    public static BaseComponent[] prepend(BaseComponent[] message, BaseComponent[] prepend) {
        List<BaseComponent> bb = new ArrayList<>();
        Collections.addAll(bb, prepend);
        Collections.addAll(bb, message);
        return bb.toArray(new BaseComponent[0]);
    }

    public static String resetColor(ChatColor c, String msg) {
        if(msg == null) return msg;
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
        sender.sendMessage(ChatColor.GOLD + "[v] " + resetColor(ChatColor.GOLD, message));
    }

    public static void sendDebug(CommandSender sender, BaseComponent[] message) {
        sender.spigot().sendMessage(prepend(message, new TextBuilder("[d] ").color(ChatColor.GOLD).create()));
    }

    public static void sendDebugVerbose(CommandSender sender, String message) {
        if (sender == null || !(sender instanceof Player)
                || !DebugFlags.is((Player) sender)
                || !DebugFlags.get((Player) sender).verbose) {
            return;
        }
        sendDebug(sender, message);
    }

    public static void sendDebugVerboseComponent(CommandSender sender, BaseComponent[] message) {
        if (sender == null || !(sender instanceof Player)
                || !DebugFlags.is((Player) sender)
                || !DebugFlags.get((Player) sender).verbose) {
            return;
        }
        sendDebug(sender, message);
    }

    public static void sendException(Module module, String err) { sendException(module, null, new Exception(err)); }
    public static void sendException(Module module, Throwable th) { sendException(module, null, th); }
    public static void sendException(Module module, CommandSender sender, String err) { sendException(module, sender, new Exception(err)); }
    public static void sendException(Module module, CommandSender sender, Throwable th) { sendException(module.getName(), sender, th, true); }

    public static void sendException(String module, String err) { sendException(module, null, new Exception(err)); }
    public static void sendException(String module, Throwable th) { sendException(module, null, th); }
    public static void sendException(String module, CommandSender sender, String err) { sendException(module, sender, new Exception(err)); }
    public static void sendException(String module, CommandSender sender, Throwable th) { sendException(module, sender, th, true); }

    private static void sendException(String module, CommandSender sender, Throwable th, boolean log) {
        String message = getThrowableMessage(th);

        for(String line : pruneStackTrace(getStackTrace(th)).split("\n"))
            LegendsOfValeros.getInstance().getLogger().severe(line);

        BaseComponent[] bc = new TextBuilder("[X" + (module != null ? ":" + module : "") + "] " + message).color(ChatColor.DARK_RED).create();

        if(sender != null)
            sender.spigot().sendMessage(bc);
        else{
            for (Player p : Bukkit.getOnlinePlayers())
                if (p.isOp()) {
                    p.spigot().sendMessage(bc);
                }
        }

        if(log && LegendsOfValeros.getMode().doLogSaving())
            sendExceptionToDiscord(module, sender, th, false);
    }

    public static void sendSevereException(Module module, String err) { sendSevereException(module, null, new Exception(err)); }
    public static void sendSevereException(Module module, Throwable th) { sendSevereException(module, null, th); }
    public static void sendSevereException(Module module, CommandSender sender, String err) { sendSevereException(module, sender, new Exception(err)); }
    public static void sendSevereException(Module module, CommandSender sender, Throwable th) { sendSevereException(module.getName(), sender, th); }

    public static void sendSevereException(String module, String err) { sendSevereException(module, null, new Exception(err)); }
    public static void sendSevereException(String module, Throwable th) { sendSevereException(module, null, th); }
    public static void sendSevereException(String module, CommandSender sender, String err) { sendSevereException(module, sender, new Exception(err)); }
    public static void sendSevereException(String module, CommandSender sender, Throwable th) {
        sendException(module, sender, th, false);

        if (LegendsOfValeros.getMode().doLogSaving()) {
            try {
                // ExceptionManager.add(module, sender, th);
                sendExceptionToDiscord(module, sender, th, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends an exception to discord into the logs channel
     * @param module
     * @param sender
     */
    private static void sendExceptionToDiscord(String module, CommandSender sender, Throwable th, boolean includeTrace) {
        if (Discord.SERVER != null) {
            // TODO: Make this channel configurable
            Channel channel = Discord.SERVER.getChannelById("358612310731915264");

            if (channel != null) {
                String message = getThrowableMessage(th);
                String trace = includeTrace ? pruneStackTrace(getStackTrace(th)) : null;

                Utilities.getInstance().getScheduler().executeInMyCircle(() -> {
                    try {
                        channel.sendMessage("`[" + Discord.TAG + (module != null ? ":" + module : "") + "]` **"
                                + (sender != null ? " **__" + sender.getName() + "__ triggered an exception: " : "")
                                + message + "**"
                                + (trace != null ? "```" + trace + "```" : "")).get();
                    } catch (InterruptedException | ExecutionException _e) {
                        _e.printStackTrace();
                    }
                });
            }
        }
    }

    private static String getThrowableMessage(Throwable th) {
        return (th.getMessage() != null ? th.getMessage() : "Something went wrong!");
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static String pruneStackTrace(String trace) {
        StringBuilder str = new StringBuilder();

        int i = -2;
        for(String line : trace.split("\n")) {
            if(i <= -1) {
                if(i++ == -2)
                    str.append(line + "\n");

                // Ignore non-LOV packages
                if (!line.contains("com.legendsofvaleros")) continue;
                // Ignore scheduler package
                if (line.contains("com.legendsofvaleros.scheduler")) continue;
                // Ignore this class
                if (line.contains("com.legendsofvaleros.util.MessageUtil")) continue;
            }

            i++;

            // LegendsOfValeros.getInstance().getLogger().warning(line);
            str.append(line + "\n");

            // Don't print too many lines. After an amount, it's just spam.
            if(i > 6 && !line.contains("legendsofvaleros")) break;
        }

        return str.toString();
    }

    public static void onEnable() {
        Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
            th.printStackTrace();

            // ExceptionManager.add(null, null, th);
        });
    }

    /*public static class ExceptionManager {
        private static final String TABLE_NAME = "exceptions";

        private static final String ID_FIELD = "exception_id";
        private static final String PLUGIN_FIELD = "plugin";
        private static final String TIME = "time";
        private static final String PLAYER = "player";
        private static final String TRACE = "stack_trace";

        private static TableManager manager;

        public static void onEnable(String dbPoolId) {

        }

        public static void add(String module, CommandSender sender, Throwable th) {
            String trace = pruneStackTrace(getStackTrace(th));

            manager.query()
                    .insert()
                    .values(PLUGIN_FIELD, module == null ? "Unknown" : module,
                            PLAYER, sender == null ? "Unknown" : sender.getName(),
                            TRACE, trace)
                    .build()
                    .execute(true);
        }
    }*/
}