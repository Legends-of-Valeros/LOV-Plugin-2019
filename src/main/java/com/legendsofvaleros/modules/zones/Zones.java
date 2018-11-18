package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Ambience;
import com.codingforcookies.ambience.PlayerAmbience;
import com.codingforcookies.ambience.Sound;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.modules.zones.quest.EnterZoneObjective;
import com.legendsofvaleros.modules.zones.quest.ExitZoneObjective;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import com.legendsofvaleros.modules.chat.Chat;
import com.legendsofvaleros.modules.chat.IChannelHandler;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggles;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map.Entry;
import java.util.UUID;

public class Zones extends ListenerModule {

    private PvPToggles toggles;
    private boolean pvpAllow;
    private byte pvpPriority;

    private static Zones instance;

    public static Zones getInstance() {
        return instance;
    }

    private static ZoneManager manager;

    public static ZoneManager manager() {
        return manager;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
        manager = new ZoneManager();
        toggles = PvP.getInstance().getToggles();

        ConfigurationSection section = getConfig().getConfigurationSection("pvp");
        pvpAllow = section != null ? section.getBoolean("allow", true) : true;
        pvpPriority = section != null ? (byte) section.getInt("pvp-priority", 0) : 0;

        Utilities.getCommandManager().loadCommandClass(ZoneCommands.class);

        ObjectiveFactory.registerType("zone_enter", EnterZoneObjective.class);
        ObjectiveFactory.registerType("zone_exit", ExitZoneObjective.class);

        Chat.getInstance().registerChannel('Z', new IChannelHandler() {
            @Override public ChatColor getTagColor() {
                return ChatColor.DARK_AQUA;
            }

            @Override public ChatColor getChatColor() {
                return ChatColor.WHITE;
            }

            @Override public String getName(Player p) {
                if (p == null) return "Zone";

                Zone zone = Zones.manager().getZone(p);
                if (zone == null) return "Unknown";

                return zone.name;
            }

            @Override public boolean canSetDefault() {
                return true;
            }

            @Override public boolean canDisable() {
                return true;
            }

            @Override
            public void onChat(Player p, FancyMessage fm) {
                Zone zone = Zones.manager().getZone(p);
                if (zone == null) {
                    MessageUtil.sendError(p, "Unable to send message. You are not in a zone!");
                    return;
                }

                Player pl;
                for (Entry<UUID, String> entry : Zones.manager().getPlayerZones()) {
                    Zone zz = Zones.manager().getZone(entry.getValue());
                    if (zz.channel.equals(zone.channel)) {
                        pl = Bukkit.getPlayer(entry.getKey());
                        if (Chat.getInstance().isChannelOn(pl, 'Z'))
                            fm.send(pl);
                    }
                }
            }
        });
    }

    @Override
    public void onUnload() {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEnterZone(ZoneEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        if (pvpAllow)
            toggles.setToggleFor(event.getPlayer().getUniqueId(), pvpPriority, event.getZone().pvp, 0);

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));

        Title title = new Title(event.getZone().name, event.getZone().subname);
        title.setTitleColor(ChatColor.GOLD);
        title.setSubtitleColor(ChatColor.WHITE);
        TitleUtil.queueTitle(title, event.getPlayer());

        PlayerAmbience a = Ambience.get(event.getPlayer());
        a.clear();

        if (event.getZone().ambience != null)
            for (Sound s : event.getZone().ambience)
                a.queueSound(s);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeaveZone(ZoneLeaveEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        if (pvpAllow)
            toggles.removeToggleFor(event.getPlayer().getUniqueId(), pvpPriority);

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}