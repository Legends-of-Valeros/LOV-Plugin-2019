package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;
import com.legendsofvaleros.modules.quests.objective.regions.ActionRegionAccess;
import com.legendsofvaleros.modules.quests.objective.regions.ActionRegionDeny;
import com.legendsofvaleros.modules.quests.objective.regions.EnterRegionObjective;
import com.legendsofvaleros.modules.quests.objective.regions.ExitRegionObjective;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;
import com.legendsofvaleros.modules.regions.Regions;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RegionIntegration extends Integration implements Listener {
    public RegionIntegration() {
        Regions.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("region_enter", EnterRegionObjective.class);
        QuestObjectiveFactory.registerType("region_exit", ExitRegionObjective.class);

        QuestActionFactory.registerType("region_access", ActionRegionAccess.class);
        QuestActionFactory.registerType("region_deny", ActionRegionDeny.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnterRegion(RegionEnterEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;

        if(event.getRegion().quests.size() > 0) {
            for(String questId : event.getRegion().quests)
                QuestController.attemptGiveQuest(Characters.getPlayerCharacter(event.getPlayer()), questId);
        }

        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaveRegion(RegionLeaveEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestManager.callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}