package com.legendsofvaleros.modules.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettings;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettingsOpenEvent;
import com.legendsofvaleros.modules.ListenerModule;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;

import mkremins.fanciful.FancyMessage;

public class Chat extends ListenerModule {

    private static Chat instance;

    public static Chat getInstance() {
        return instance;
    }

    @Override public void onLoad() {
        instance = this;
        Discord.onEnable();

        //TODO add channel enum
        registerChannel('W', new IChannelHandler() {
            @Override public ChatColor getTagColor() {
                return ChatColor.GRAY;
            }

            @Override public ChatColor getChatColor() {
                return ChatColor.GRAY;
            }

            @Override public String getName(Player p) {
                return "World";
            }

            @Override public boolean canSetDefault() {
                return false;
            }

            @Override public boolean canDisable() {
                return true;
            }

            @Override
            public void onChat(Player p, FancyMessage fm) {
                for (Player pl : Bukkit.getOnlinePlayers())
                    if (isChannelOn(pl, 'W'))
                        fm.send(pl);
            }
        });
        registerChannel('T', new IChannelHandler() {
            @Override public ChatColor getTagColor() {
                return ChatColor.GREEN;
            }

            @Override public ChatColor getChatColor() {
                return ChatColor.GREEN;
            }

            @Override public String getName(Player p) {
                return "Trade";
            }

            @Override public boolean canSetDefault() {
                return false;
            }

            @Override public boolean canDisable() {
                return true;
            }

            @Override
            public void onChat(Player p, FancyMessage fm) {
                for (Player pl : Bukkit.getOnlinePlayers())
                    if (isChannelOn(pl, 'T'))
                        fm.send(pl);
            }
        });
        registerChannel('L', new IChannelHandler() {
            @Override public ChatColor getTagColor() {
                return ChatColor.WHITE;
            }

            @Override public ChatColor getChatColor() {
                return ChatColor.WHITE;
            }

            @Override public String getName(Player p) {
                return "Local";
            }

            @Override public boolean canSetDefault() {
                return true;
            }

            @Override public boolean canDisable() {
                return false;
            }

            @Override
            public void onChat(Player p, FancyMessage fm) {
                for (Player pl : Bukkit.getOnlinePlayers())
                    if (pl.getLocation().distance(p.getLocation()) < 25)
                        fm.send(pl);
            }
        });

        Bukkit.getPluginManager().registerEvents(this, LegendsOfValeros.getInstance());
    }

    @Override public void onUnload() {

    }

    private char channelDefault = 'L';

    public void setChannelDefault(char channelId) {
        channelDefault = channelId;
    }

    public char getChannelDefault() {
        return channelDefault;
    }

    protected final Map<Character, IChannelHandler> channels = new HashMap<>();

    public void registerChannel(Character c, IChannelHandler handler) {
        channels.put(c, handler);
    }

    private final Map<UUID, PlayerChat> players = new HashMap<>();

    public boolean isChannelOn(Player p, char id) {
        return !this.players.get(p.getUniqueId()).offChannels.contains(id);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent e) {
        if (e.getCommand().startsWith("say")) {
            e.setCancelled(true);

            FancyMessage fm = new FancyMessage("");
            fm.then("Mysterious Voice").color(ChatColor.DARK_GRAY).style(ChatColor.BOLD);
            fm.then(": ").color(ChatColor.DARK_GRAY);
            fm.then(e.getCommand().split(" ", 2)[1]).color(ChatColor.GRAY);

            Bukkit.getLogger().info(ChatColor.stripColor(fm.toOldMessageFormat()));

            for (Player p : Bukkit.getOnlinePlayers())
                fm.send(p);
        } else if (e.getCommand().startsWith("restart")) {
            e.setCancelled(true);

            e.getSender().sendMessage("Use .restart to restart the server.");
        } else if (e.getCommand().startsWith("reload")) {
            e.setCancelled(true);

            e.getSender().sendMessage("Don't do that.");
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
                        }, Utilities.syncExecutor());

                        return;
                    }
                }

                if (isChannelOn(e.getPlayer(), channelId)) {
                    Character oldChannel = (data.channel == null ? channelDefault : data.channel);
                    data.channel = channelId;

                    e.getPlayer().chat(e.getMessage().substring(3));

                    data.channel = oldChannel;
                } else
                    MessageUtil.sendError(e.getPlayer(), "You may not speak in a channel you have disabled. Type '/" + channelId + " on' to re-enable it.");
            } else {
                IChannelHandler ch = channels.get(channelId);

                if (!ch.canSetDefault()) {
                    MessageUtil.sendError(e.getPlayer(), "You are not allowed to set that channel as default.");
                } else {
                    data.channel = channelId;

                    PlayerSettings.get(e.getPlayer()).get().put("chat.selected", String.valueOf(channelId));

                    MessageUtil.sendUpdate(e.getPlayer(), "You are now speaking in " + ch.getName(null) + " chat.");
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
        }, Utilities.syncExecutor());

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
        IChannelHandler ch = channels.get(data.channel);

        FancyMessage fm = new FancyMessage("");
        {
            fm.then(data.channel + " ").color(ch.getTagColor()).style(ChatColor.BOLD).tooltip(ch.getName(e.getPlayer()));

            if (data.prefix != null) {
                fm.then(ChatColor.translateAlternateColorCodes('&', data.prefix));
                if (data.title != null)
                    fm.tooltip(data.title);
            }

            fm.then(e.getPlayer().getName());

            if (!Characters.isPlayerCharacterLoaded(e.getPlayer())) {
                fm.color(ChatColor.GRAY).style(ChatColor.ITALIC);
            } else {
                PlayerCharacter pc = Characters.getPlayerCharacter(e.getPlayer());
                if (pc != null) {
                    fm.color(pc.getPlayerClass().getColor())
                            .formattedTooltip(
                                    new FancyMessage("Race: ").color(ChatColor.YELLOW)
                                            .then(pc.getPlayerRace().getUserFriendlyName() + "\n")
                                            .then("Class: ").color(ChatColor.YELLOW)
                                            .then(pc.getPlayerClass().getUserFriendlyName() + "\n")
                                            .then("Level: ").color(ChatColor.YELLOW)
                                            .then(String.valueOf(pc.getExperience().getLevel()))
                            );
                }
            }

            if (data.suffix != null)
                fm.then(ChatColor.translateAlternateColorCodes('&', data.suffix));

            fm.then(": ").color(ChatColor.DARK_GRAY);
            fm.then(e.getMessage()).color(ch.getChatColor());
        }

        Bukkit.getLogger().info(ChatColor.stripColor(fm.toOldMessageFormat()));

        ch.onChat(e.getPlayer(), fm);

        Discord.onChat(e, data);
    }
}