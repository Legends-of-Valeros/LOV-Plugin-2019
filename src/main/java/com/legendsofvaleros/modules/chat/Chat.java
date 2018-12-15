package com.legendsofvaleros.modules.chat;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.parties.Parties;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettings;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettingsOpenEvent;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.util.Discord;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.PlayerData;
import com.legendsofvaleros.util.TextBuilder;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
public class Chat extends ModuleListener {
    private static Chat instance;
    public static Chat getInstance() { return instance; }

    private static Map<Character, String> chanToDiscord = new HashMap<>();
    private static Map<String, Character> discordToChan = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        for (ChatChannel channel : ChatChannel.values()) {
            channels.put(channel.getPrefix(), channel);
        }

        ConfigurationSection config = getConfig().getConfigurationSection("discord");

        getLogger().info("" + getConfig().getKeys(false));
        ConfigurationSection channels = config.getConfigurationSection("channels");
        for (String key : channels.getKeys(false)) {
            if (key.length() != 1) continue;
            chanToDiscord.put(key.charAt(0), channels.getString(key));
            discordToChan.put(channels.getString(key), key.charAt(0));
        }

    }

    private void onChat(Player p, BaseComponent[] bc, ChatChannel sendTo) {
        switch (sendTo) {
            case TRADE:
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (isChannelOn(pl, 'T')) {
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
                    if (isChannelOn(pl, 'W')) {
                        pl.spigot().sendMessage(bc);
                    }
                }
            case ZONE:
                Zones.getInstance().onChat(p, bc);
                break;
            case PARTY:
                Parties.getInstance().onChat(p, bc);
                break;
        }

    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    private ChatChannel channelDefault = ChatChannel.LOCAL;

    private final Map<Character, ChatChannel> channels = new HashMap<>();

    private final Map<UUID, PlayerChat> players = new HashMap<>();

    public boolean isChannelOn(Player p, char id) {
        return !this.players.get(p.getUniqueId()).offChannels.contains(id);
    }

    @EventHandler
    public void onDiscordConnected(Discord.ConnectedEvent e) {
        final Server server = e.getServer();
        final DiscordAPI api = e.getAPI();

        api.registerListener((MessageCreateListener) (api1, message) -> {
            if (message.getAuthor().isYourself() || message.getAuthor().isBot() || message.isPrivateMessage() ||
                    message.getContent().trim().length() == 0 ||
                    message.getContent().startsWith("/") ||
                    !discordToChan.containsKey(message.getChannelReceiver().getId())) {
                return;
            }

            char channelId = discordToChan.get(message.getChannelReceiver().getId());
            ChatChannel ch = Chat.getInstance().channels.get(channelId);

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent e) {
        if (e.getCommand().startsWith("say")) {
            e.setCancelled(true);

            TextBuilder tb = new TextBuilder("");
            tb.append("Mysterious Voice").color(ChatColor.DARK_GRAY).bold(true);
            tb.append(": ").color(ChatColor.DARK_GRAY);
            tb.append(e.getCommand().split(" ", 2)[1]).color(ChatColor.GRAY);
            BaseComponent[] bc = tb.create();

            Bukkit.getConsoleSender().spigot().sendMessage(bc);

            for (Player p : Bukkit.getOnlinePlayers())
                p.spigot().sendMessage(bc);
        }
    }

    @EventHandler
    public void onSettingsOpen(PlayerSettingsOpenEvent event) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) throws InterruptedException, ExecutionException {
        String[] query = e.getMessage().substring(1).split(" ", 2);
        if (query[0].length() != 1) return;

        Character channelId = query[0].toUpperCase().charAt(0);
        if (channels.containsKey(channelId)) {
            e.setCancelled(true);

            PlayerChat data = players.get(e.getPlayer().getUniqueId());

            if (query.length > 1) {
                if (query.length == 2) {
                    if (query[1].equals("off") || query[1].equals("on")) {
                        if (query[1].equals("off")) {
                            if (!isChannelOn(e.getPlayer(), channelId))
                                MessageUtil.sendUpdate(e.getPlayer(), "That channel is already off.");
                            else {
                                data.offChannels.add(channelId);
                                MessageUtil.sendUpdate(e.getPlayer(), "You will no longer receive messages from that channel.");
                            }
                        } else {
                            if (isChannelOn(e.getPlayer(), channelId))
                                MessageUtil.sendUpdate(e.getPlayer(), "That channel is already on.");
                            else {
                                data.offChannels.remove(channelId);
                                MessageUtil.sendUpdate(e.getPlayer(), "You will now receive messages from that channel.");
                            }
                        }

                        ListenableFuture<PlayerSettings> future = PlayerSettings.get(e.getPlayer());
                        future.addListener(() -> {
                            try {
                                PlayerSettings settings = future.get();
                                if (data.offChannels.size() == 0)
                                    settings.remove("chat.off");
                                else
                                    settings.put("chat.off", String.join("", data.offChannels.toArray(new String[data.offChannels.size()])));
                            } catch (InterruptedException | ExecutionException e1) {
                                e1.printStackTrace();
                            }
                        }, getScheduler()::sync);

                        return;
                    }
                }

                if (isChannelOn(e.getPlayer(), channelId)) {
                    Character oldChannel = (data.channel == null ? channelDefault.getPrefix() : data.channel);
                    data.channel = channelId;

                    e.getPlayer().chat(e.getMessage().substring(3));

                    data.channel = oldChannel;
                } else
                    MessageUtil.sendError(e.getPlayer(), "You may not speak in a channel you have disabled. Type '/" + channelId + " on' to re-enable it.");
            } else {
                ChatChannel ch = channels.get(channelId);

                if (!ch.isCanSetDefault()) {
                    MessageUtil.sendError(e.getPlayer(), "You are not allowed to set that channel as default.");
                } else {
                    data.channel = channelId;
                    PlayerSettings.get(e.getPlayer()).get().put("chat.selected", String.valueOf(channelId));
                    MessageUtil.sendUpdate(e.getPlayer(), "You are now speaking in " + ch.getName() + " chat.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        PlayerChat data = new PlayerChat();
        for (PermissionAttachmentInfo entry : event.getPlayer().getEffectivePermissions()) {
            if (entry.getPermission().startsWith("title.")) {
                if (data.title == null)
                    data.title = WordUtils.capitalizeFully(entry.getPermission().split("title.", 2)[1]);

            } else if (entry.getPermission().startsWith("prefix.")) {
                if (data.prefix == null)
                    data.prefix = WordUtils.capitalizeFully(entry.getPermission().split("prefix.", 2)[1]);

            } else if (entry.getPermission().startsWith("suffix.")) {
                if (data.suffix == null)
                    data.suffix = WordUtils.capitalizeFully(entry.getPermission().split("suffix.", 2)[1]);
            }
        }

        ListenableFuture<PlayerSettings> future = PlayerSettings.get(event.getPlayer());
        future.addListener(() -> {
            try {
                PlayerSettings settings = future.get();
                if (settings.containsKey("chat.selected") && settings.get("chat.selected") != null)
                    data.channel = settings.get("chat.selected").charAt(0);

                if (settings.containsKey("chat.off") && settings.get("chat.off") != null)
                    for (char c : settings.get("chat.off").toCharArray())
                        data.offChannels.add(c);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, getScheduler()::sync);

        players.put(event.getPlayer().getUniqueId(), data);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        players.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);

        PlayerChat data = players.get(e.getPlayer().getUniqueId());
        ChatChannel ch = channels.get(data.channel);

        TextBuilder tb = new TextBuilder("");
        {
            tb.append(data.channel + " ").color(ch.getTagColor()).bold(true).hover(ch.getName());

            Guild g = Guild.getGuildByMember(e.getPlayer().getUniqueId());
            if(g != null && g.getTag() != null) {
                tb.append(g.getTag()).bold(true).hover(g.getName());
                tb.append(" ");
            }

            if (data.prefix != null) {
                tb.append(ChatColor.translateAlternateColorCodes('&', data.prefix));
                if (data.title != null)
                    tb.hover(data.title);
            }

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

    private void sendDiscordMessage(AsyncPlayerChatEvent e, PlayerChat data) {
        if (chanToDiscord.containsKey(data.channel)) {
            if (Discord.SERVER != null) {
                String channelId = chanToDiscord.get(data.channel);
                if (channelId != null) {
                    Channel channel = Discord.SERVER.getChannelById(channelId);

                    if (channel != null) {
                        getScheduler().executeInMyCircle(() -> {
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
