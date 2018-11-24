package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Ambience;
import com.codingforcookies.ambience.PlayerAmbience;
import com.codingforcookies.ambience.Sound;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.Chat;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvP;
import com.legendsofvaleros.modules.pvp.toggle.PvPToggles;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.modules.zones.quest.EnterZoneObjective;
import com.legendsofvaleros.modules.zones.quest.ExitZoneObjective;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map.Entry;
import java.util.UUID;

@DependsOn(PvP.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Chat.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
@DependsOn(Quests.class)
public class Zones extends ModuleListener {

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
        pvpAllow = section == null || section.getBoolean("allow", true);
        pvpPriority = section != null ? (byte) section.getInt("pvp-priority", 0) : 0;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ZoneCommands());

        ObjectiveFactory.registerType("zone_enter", EnterZoneObjective.class);
        ObjectiveFactory.registerType("zone_exit", ExitZoneObjective.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEnterZone(ZoneEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        if (pvpAllow)
            toggles.setToggleFor(event.getPlayer().getUniqueId(), pvpPriority, event.getZone().pvp, 0);

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));

        Title title = new Title(event.getZone().name, event.getZone().subname);
        title.setTitleColor(org.bukkit.ChatColor.GOLD);
        title.setSubtitleColor(org.bukkit.ChatColor.WHITE);
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

    public void onChat(Player p, BaseComponent[] bc) {
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
                    pl.spigot().sendMessage(bc);
            }
        }
    }
}