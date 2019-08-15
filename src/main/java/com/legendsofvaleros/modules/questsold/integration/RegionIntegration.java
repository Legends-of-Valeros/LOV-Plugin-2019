package com.legendsofvaleros.modules.questsold.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.questsold.QuestController;
import com.legendsofvaleros.modules.questsold.action.regions.ActionRegionAccess;
import com.legendsofvaleros.modules.questsold.action.regions.ActionRegionDeny;
import com.legendsofvaleros.modules.questsold.objective.regions.EnterRegionObjective;
import com.legendsofvaleros.modules.questsold.objective.regions.ExitRegionObjective;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RegionIntegration extends Integration implements Listener {
    public RegionIntegration() {
        RegionController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("region_enter", EnterRegionObjective.class);
        QuestObjectiveFactory.registerType("region_exit", ExitRegionObjective.class);

        QuestActionFactory.registerType("region_access", ActionRegionAccess.class);
        QuestActionFactory.registerType("region_deny", ActionRegionDeny.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnterRegion(RegionEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        if(event.getRegion().quests != null) {
            for(String questId : event.getRegion().quests)
                QuestController.getInstance().attemptGiveQuest(Characters.getPlayerCharacter(event.getPlayer()), questId);
        }

        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaveRegion(RegionLeaveEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}