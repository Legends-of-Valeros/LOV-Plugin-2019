package com.legendsofvaleros.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.chat.Chat;
import com.legendsofvaleros.modules.chat.IChannelHandler;
import com.legendsofvaleros.modules.chat.PlayerChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.util.concurrent.FutureCallback;
import com.legendsofvaleros.util.PlayerData;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import mkremins.fanciful.FancyMessage;
import org.bukkit.plugin.java.JavaPlugin;

public class Discord {
    public static boolean LINKED = false;
    public static DiscordAPI API;

    public static Server SERVER;
    public static String TAG;

    public static void onEnable() {
        ConfigurationSection config = LegendsOfValeros.getInstance().getConfig().getConfigurationSection("discord");

        TAG = config.getString("tag");
        if(TAG == null)
            TAG = "unknown";

        API = Javacord.getApi(config.getString("token"), true);
        API.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                LegendsOfValeros.getInstance().getLogger().info("Link established with Discord bot: " + api.getYourself().getName());

                LINKED = true;
                SERVER = api.getServerById(config.getString("server"));

                Bukkit.getServer().getPluginManager().callEvent(new Discord.ConnectedEvent(SERVER, api));
            }

            @Override
            public void onFailure(Throwable t) {
                LegendsOfValeros.getInstance().getLogger().severe("Failed to establish link with discord bot.");

                t.printStackTrace();
            }
        });
    }

    public static class ConnectedEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        @Override public HandlerList getHandlers() { return handlers; }

        final Server server;
        public Server getServer() { return server; }

        final DiscordAPI api;
        public DiscordAPI getAPI() { return api; }

        public ConnectedEvent(Server server, DiscordAPI api) {
            this.server = server;
            this.api = api;
        }
    }
}