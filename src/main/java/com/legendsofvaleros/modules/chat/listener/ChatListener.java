package com.legendsofvaleros.modules.chat.listener;

import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.ChatChannel;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.chat.PlayerChat;
import com.legendsofvaleros.modules.guilds.GuildController;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.util.Discord;
import com.legendsofvaleros.util.PlayerData;
import com.legendsofvaleros.util.TextBuilder;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.ExecutionException;

public class ChatListener implements Listener {
    private ChatController chat = ChatController.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }

        //player is in an auction prompt and therefore the message should not be sent
        if (AuctionController.getInstance().isPrompted(e.getPlayer())) {
            return;
        }

        e.setCancelled(true);

        PlayerChat data = chat.getPlayer(e.getPlayer().getUniqueId());
        ChatChannel ch = chat.getChannel(data.channel);

        TextBuilder tb = new TextBuilder("");
        {
            tb.append(data.channel + " ").color(ch.getTagColor()).bold(true).hover(ch.getName());

            // Add permission prefix
            if (data.prefix != null) {
                tb.append(ChatColor.translateAlternateColorCodes('&', data.prefix));
                if (data.title != null)
                    tb.hover(data.title);
            }

            if(Modules.isLoaded(GuildController.class)) {
                // Guild tag goes after permission prefix
                Guild g = Guild.getGuildByMember(e.getPlayer().getUniqueId());
                if(g != null && g.getTag() != null) {
                    tb.append(g.getTag()).bold(true)
                            .hover(g.getName(),
                                    ChatColor.GRAY + g.getMember(e.getPlayer().getUniqueId()).getRole().getName());
                    tb.append(" ");
                }
            }

            // Player titles go here

            tb.append(e.getPlayer().getName());

            if (!Characters.isPlayerCharacterLoaded(e.getPlayer())) {
                tb.color(ChatColor.GRAY).italic(true);
            } else {
                PlayerCharacter pc = Characters.getPlayerCharacter(e.getPlayer());
                if (pc != null) {
                    tb.color(pc.getPlayerClass().getColor()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new TextBuilder("Race: ").color(ChatColor.YELLOW)
                                    .append(pc.getPlayerRace().getUserFriendlyName() + "\n")
                                    .append("Class: ").color(ChatColor.YELLOW)
                                    .append(pc.getPlayerClass().getUserFriendlyName() + "\n")
                                    .append("Level: ").color(ChatColor.YELLOW)
                                    .append(String.valueOf(pc.getExperience().getLevel())).create()
                    ));
                }
            }

            if (data.suffix != null)
                tb.append(ChatColor.translateAlternateColorCodes('&', data.suffix));

            tb.append(": ").color(ChatColor.DARK_GRAY);
            tb.append(e.getMessage()).color(ch.getChatColor());
        }

        BaseComponent[] bc = tb.create();
        Bukkit.getConsoleSender().spigot().sendMessage(bc);

        onChat(e.getPlayer(), bc, ch);
        sendDiscordMessage(e, data);
    }

    @EventHandler
    public void onDiscordConnected(Discord.ConnectedEvent e) {
        final Server server = e.getServer();
        final DiscordAPI api = e.getAPI();

        api.registerListener((MessageCreateListener) (api1, message) -> {
            if (message.getAuthor().isYourself() || message.getAuthor().isBot() || message.isPrivateMessage() ||
                    message.getContent().trim().length() == 0 ||
                    message.getContent().startsWith("/") ||
                    chat.getDiscordToChannel(message.getChannelReceiver().getId()) == null) {
                return;
            }

            char channelId = chat.getDiscordToChannel(message.getChannelReceiver().getId());
            ChatChannel ch = chat.getChannel(channelId);

            PlayerData data = null;
            try {
                data = PlayerData.getByDiscordID(message.getAuthor().getId()).get();
            } catch (InterruptedException | ExecutionException ee) {
                ee.printStackTrace();
            }

            TextBuilder tb = new TextBuilder("");

            tb.append(channelId + " ").color(ch.getTagColor()).bold(true).hover(ch.getName());

            tb.append(data != null ? data.username :
                    (message.getAuthor().hasNickname(server) ? message.getAuthor().getNickname(server) : message.getAuthor().getName()))
                    .color(ChatColor.GRAY).italic(true);

            if (data == null)
                tb.underlined(true).hover("Unverified Discord");
            else
                tb.hover("Verified Discord");

            tb.append(": ").color(ChatColor.DARK_GRAY);
            tb.append(message.getContent()).color(ch.getChatColor());

            onChat(null, tb.create(), ch);
        });
    }

    private void onChat(Player p, BaseComponent[] bc, ChatChannel sendTo) {
        switch (sendTo) {
            case TRADE:
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (chat.isChannelOn(pl, 'T')) {
                        pl.spigot().sendMessage(bc);
                    }
                }
                break;
            case LOCAL:
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (pl.getLocation().distance(p.getLocation()) < 25) {
                        pl.spigot().sendMessage(bc);
                    }
                }
                break;
            case WORLD:
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (chat.isChannelOn(pl, 'W')) {
                        pl.spigot().sendMessage(bc);
                    }
                }
                break;
            case ZONE:
                ZonesController.getInstance().onChat(p, bc);
                break;
            case PARTY:
                PartiesController.getInstance().onChat(p, bc);
                break;
        }
    }

    private void sendDiscordMessage(AsyncPlayerChatEvent e, PlayerChat data) {
        if (chat.getChannelToDiscord(data.channel) != null) {
            if (Discord.SERVER != null) {
                String channelId = chat.getChannelToDiscord(data.channel);
                if (channelId != null) {
                    Channel channel = Discord.SERVER.getChannelById(channelId);

                    if (channel != null) {
                        ChatController.getInstance().getScheduler().executeInMyCircle(() -> {
                            try {
                                channel.sendMessage("`[" + Discord.TAG + "]` **" + e.getPlayer().getName() + "**: " + e.getMessage()).get();
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