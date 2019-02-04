package com.legendsofvaleros.modules.zones;

import com.codingforcookies.ambience.Ambience;
import com.codingforcookies.ambience.PlayerAmbience;
import com.codingforcookies.ambience.Sound;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.pvp.PvPController;
import com.legendsofvaleros.modules.pvp.event.PvPCheckEvent;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.modules.zones.quest.EnterZoneObjective;
import com.legendsofvaleros.modules.zones.quest.ExitZoneObjective;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@DependsOn(PvPController.class)
@DependsOn(PlayerMenu.class)
@DependsOn(ChatController.class)
@DependsOn(CombatEngine.class)
@DependsOn(Characters.class)
// TODO: Create subclass for listeners?
public class ZonesController extends ModuleListener {
    private static ZonesController instance;

    public static ZonesController getInstance() {
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

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new ZoneCommands());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEnterZone(ZoneEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));

        boolean pvp = PvPController.getInstance().isPvPEnabled() && event.getZone().pvp;

        Title title = new Title(event.getZone().name, event.getZone().subname + (pvp ? ChatColor.RED + "(pvp enabled)" : ""));
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

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler
    public void isPvPAllowed(PvPCheckEvent event) {
        // Zones should never override a PvP check.
        if (event.isCancelled()) return;

        if (!manager.getZone(event.getAttacker()).pvp
                || !manager.getZone(event.getDamaged()).pvp) {
            event.setCancelled(true);
        }
    }
}