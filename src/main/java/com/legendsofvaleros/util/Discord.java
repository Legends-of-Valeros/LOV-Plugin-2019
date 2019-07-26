package com.legendsofvaleros.util;

import com.google.common.util.concurrent.FutureCallback;
import com.legendsofvaleros.scheduler.InternalTask;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.concurrent.Future;

public class Discord {
    private static boolean LINKED = false;
    public static DiscordAPI API;

    public static Server SERVER;
    public static String TAG;

    private static Channel logsChannel;

    public static void onEnable() {
        ConfigurationSection config = Utilities.getInstance().getConfig().getConfigurationSection("discord");

        TAG = config.getString("tag");
        if (TAG == null) {
            TAG = "unknown";
        }

        API = Javacord.getApi(config.getString("token"), true);
        API.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                Utilities.getInstance().getLogger().info("Link established with Discord bot: " + api.getYourself().getName());

                LINKED = true;
                SERVER = api.getServerById(config.getString("server"));

                Utilities.getInstance().getScheduler().executeInSpigotCircle(new InternalTask(() -> {
                    Bukkit.getServer().getPluginManager().callEvent(new Discord.ConnectedEvent(SERVER, api));
                }));
            }

            @Override
            public void onFailure(Throwable t) {
                Utilities.getInstance().getLogger().severe("Failed to establish link with discord bot.");

                t.printStackTrace();
            }
        });
    }


    /**
     * Sends a message to the #logs channel
     * @param moduleName
     * @param message
     * @return
     */
    public static Future<Message> sendLogMessage(String moduleName, String message) {
        Channel channel = Discord.getLogsChannel();
        if (channel == null) {
            MessageUtil.sendSevereException("Discord", "Could not find the logs channel. Check the permission");
            return null;
        }

        return sendMessage(channel, moduleName, message);
    }

    /**
     * Sends a message to the given discord channel id
     * @param message
     * @param channelId
     * @return
     */
    public static Future<Message> sendMessageToChannelId(String message, String moduleName, String channelId) {
        Channel channel = Discord.API.getChannelById(channelId);
        if (channel == null) {
            MessageUtil.sendException("Discord", "Could not find channel by id -" + channelId);
            return null;
        }

        return sendMessage(channel, moduleName, message);
    }

    /**
     * Sends a message to the given discord channel name
     * @param message
     * @param channelName
     * @return
     */
    public static Future<Message> sendMessageToChannelName(String message, String moduleName, String channelName) {
        Channel channelTo = null;
        for (Channel channel : Discord.API.getChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                channelTo = channel;
                break;
            }
        }
        if (channelTo == null) {
            MessageUtil.sendException("Discord", "Could not find channel by name -" + channelName);
            return null;
        }

        return sendMessage(channelTo, moduleName, message);
    }

    /**
     * Final interaction which sends the discord message directly to the channel
     * @param channel
     * @param moduleName
     * @param message
     * @return
     */
    private static Future<Message> sendMessage(Channel channel, String moduleName, String message) {
        return channel.sendMessage("`[" + Discord.TAG + (moduleName.isEmpty() ? "" : moduleName) + "]` **" + message + "**");
    }

    /**
     * Returns the id of the logs channel
     * @return
     */
    public static Channel getLogsChannel() {
        if (logsChannel == null) {
            for (Channel channel : Discord.API.getChannels()) {
                if (channel.getName().equalsIgnoreCase("logs")) {
                    logsChannel = channel;
                }
            }
        }
        return logsChannel;
    }

    public static boolean isLinked() {
        return LINKED;
    }

    public static class ConnectedEvent extends Event {
        private static final HandlerList handlers = new HandlerList();

        final Server server;
        final DiscordAPI api;

        public ConnectedEvent(Server server, DiscordAPI api) {
            this.server = server;
            this.api = api;
        }

        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }


        public Server getServer() {
            return server;
        }


        public DiscordAPI getAPI() {
            return api;
        }

    }

}