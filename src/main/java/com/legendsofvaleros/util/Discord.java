package com.legendsofvaleros.util;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Server;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class Discord {
    public static boolean LINKED = false;
    public static DiscordAPI API;

    public static Server SERVER;
    public static String TAG;

    public static void onEnable() {
        ConfigurationSection config = Utilities.getInstance().getConfig().getConfigurationSection("discord");

        TAG = config.getString("tag");
        if(TAG == null)
            TAG = "unknown";

        API = Javacord.getApi(config.getString("token"), true);
        API.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                Utilities.getInstance().getLogger().info("Link established with Discord bot: " + api.getYourself().getName());

                LINKED = true;
                SERVER = api.getServerById(config.getString("server"));

                Bukkit.getServer().getPluginManager().callEvent(new Discord.ConnectedEvent(SERVER, api));
            }

            @Override
            public void onFailure(Throwable t) {
                Utilities.getInstance().getLogger().severe("Failed to establish link with discord bot.");

                t.printStackTrace();
            }
        });
    }

    public static class ConnectedEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        @Override public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }

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