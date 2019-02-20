package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.quests.objective.zones.EnterZoneObjective;
import com.legendsofvaleros.modules.quests.objective.zones.ExitZoneObjective;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ZonesIntegration extends Integration implements Listener {
    public ZonesIntegration() {
        QuestController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("zone_enter", EnterZoneObjective.class);
        QuestObjectiveFactory.registerType("zone_exit", ExitZoneObjective.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEnterZone(ZoneEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeaveZone(ZoneLeaveEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}
