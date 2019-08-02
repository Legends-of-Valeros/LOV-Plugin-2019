package com.legendsofvaleros.modules.chat;

import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.listener.ChatListener;
import com.legendsofvaleros.modules.chat.listener.CommandListener;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@ModuleInfo(name = "Chat", info = "")
public class ChatController extends ListenerModule {
    private static ChatController instance;

    public static ChatController getInstance() {
        return instance;
    }

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
}
