package com.legendsofvaleros.modules.chat;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.auction.AuctionController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.listener.ChatListener;
import com.legendsofvaleros.modules.chat.listener.CommandListener;
import com.legendsofvaleros.modules.guilds.guild.Guild;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettings;
import com.legendsofvaleros.modules.playermenu.settings.PlayerSettingsOpenEvent;
import com.legendsofvaleros.modules.zones.ZonesController;
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
public class ChatController extends ModuleListener {
    private static ChatController instance;
    public static ChatController getInstance() { return instance; }

    private static Map<Character, String> chanToDiscord = new HashMap<>();
    public String getChannelToDiscord(Character c) { return chanToDiscord.get(c); }

    private static Map<String, Character> discordToChan = new HashMap<>();
    public Character getDiscordToChannel(String id) { return discordToChan.get(id); }

    private final ChatChannel channelDefault = ChatChannel.LOCAL;
    public ChatChannel getDefaultChannel() { return channelDefault; }

    private final Map<Character, ChatChannel> channels = new HashMap<>();
    public ChatChannel getChannel(char c) { return channels.get(c); }

    private final Map<UUID, PlayerChat> players = new HashMap<>();
    public PlayerChat getPlayer(UUID playerId) { return players.get(playerId); }
    public boolean isChannelOn(Player p, char id) {
        return !this.players.get(p.getUniqueId()).offChannels.contains(id);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        for (ChatChannel channel : ChatChannel.values()) {
            channels.put(channel.getPrefix(), channel);
        }

        registerEvents(new ChatListener());
        registerEvents(new CommandListener());

        ConfigurationSection config = getConfig().getConfigurationSection("discord");

        getLogger().info("" + getConfig().getKeys(false));
        ConfigurationSection channels = config.getConfigurationSection("channels");
        for (String key : channels.getKeys(false)) {
            if (key.length() != 1) continue;
            chanToDiscord.put(key.charAt(0), channels.getString(key));
            discordToChan.put(channels.getString(key), key.charAt(0));
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
        }, ChatController.getInstance().getScheduler()::sync);

        players.put(event.getPlayer().getUniqueId(), data);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        players.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    @EventHandler
    public void onSettingsOpen(PlayerSettingsOpenEvent event) {

    }
}
