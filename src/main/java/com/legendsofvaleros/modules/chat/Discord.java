package com.legendsofvaleros.modules.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.util.concurrent.FutureCallback;
import com.legendsofvaleros.util.PlayerData;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import mkremins.fanciful.FancyMessage;

public class Discord {
    public static boolean LINKED = false;
    public static DiscordAPI API;

    public static Server SERVER;

    public static Map<Character, String> chanToDiscord = new HashMap<>();
    public static Map<String, Character> discordToChan = new HashMap<>();

    public static String tag;

    public static void onEnable() {
        ConfigurationSection config = LegendsOfValeros.getInstance().getConfig().getConfigurationSection("discord");

        tag = config.getString("tag");

        ConfigurationSection channels = config.getConfigurationSection("channels");
        for (String key : channels.getKeys(false)) {
            if (key.length() != 1) continue;
            chanToDiscord.put(key.charAt(0), channels.getString(key));
            discordToChan.put(channels.getString(key), key.charAt(0));
        }

        API = Javacord.getApi(config.getString("token"), true);
        API.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                LegendsOfValeros.getInstance().getLogger().info("Link established with Discord bot: " + api.getYourself().getName());

                LINKED = true;
                SERVER = api.getServerById(config.getString("server"));

                api.registerListener((MessageCreateListener) (api1, message) -> {
                    if (message.getAuthor().isYourself() || message.getAuthor().isBot() || message.isPrivateMessage())
                        return;
                    if (message.getContent().trim().length() == 0) return;
                    if (message.getContent().startsWith("/")) return;
                    if (!discordToChan.containsKey(message.getChannelReceiver().getId())) return;

                    char channelId = discordToChan.get(message.getChannelReceiver().getId());
                    IChannelHandler ch = Chat.getInstance().channels.get(channelId);

                    PlayerData data = null;
                    try {
                        data = PlayerData.getByDiscordID(message.getAuthor().getId()).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    FancyMessage fm = new FancyMessage("");

                    fm.then(channelId + " ").color(ch.getTagColor()).style(ChatColor.BOLD).tooltip(ch.getName(null));

                    fm.then(data != null ? data.username :
                            (message.getAuthor().hasNickname(SERVER) ? message.getAuthor().getNickname(SERVER) : message.getAuthor().getName()))
                            .color(ChatColor.GRAY).style(ChatColor.ITALIC);

                    if (data == null)
                        fm.style(ChatColor.UNDERLINE).tooltip("Unverified Discord");
                    else
                        fm.tooltip("Verified Discord");

                    fm.then(": ").color(ChatColor.DARK_GRAY);
                    fm.then(message.getContent()).color(ch.getChatColor());

                    ch.onChat(null, fm);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                LegendsOfValeros.getInstance().getLogger().severe("Failed to establish link with discord bot.");

                t.printStackTrace();
            }
        });
    }

    public static void onChat(AsyncPlayerChatEvent e, PlayerChat data) {
        if (chanToDiscord.containsKey(data.channel)) {
            if (SERVER != null) {
                String channelId = chanToDiscord.get(data.channel);
                if (channelId != null) {
                    Channel channel = SERVER.getChannelById(channelId);

                    if (channel != null) {
                        Bukkit.getScheduler().runTaskAsynchronously(LegendsOfValeros.getInstance(), () -> {
                            try {
                                channel.sendMessage((tag != null ? "`[" + tag + "]` " : "") + "**" + e.getPlayer().getName() + "**: " + e.getMessage()).get();
                            } catch (InterruptedException | ExecutionException _e) {
                                _e.printStackTrace();
                            }
                        });
                    }
                }
            }
        }
    }
}