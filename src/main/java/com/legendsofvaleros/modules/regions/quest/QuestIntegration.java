package com.legendsofvaleros.modules.regions.quest;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.QuestActionFactory;
import com.legendsofvaleros.modules.quests.objective.stf.QuestObjectiveFactory;
import com.legendsofvaleros.modules.regions.Regions;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestIntegration extends Integration implements Listener {
    public QuestIntegration() {
        Regions.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("region_enter", EnterRegionObjective.class);
        QuestObjectiveFactory.registerType("region_exit", ExitRegionObjective.class);

        QuestActionFactory.registerType("region_access", ActionRegionAccess.class);
        QuestActionFactory.registerType("region_deny", ActionRegionDeny.class);
    }

    @EventHandler
    public void onEnterRegion(RegionEnterEvent event) {
        if(event.getRegion().quests.size() > 0) {
            for(String questId : event.getRegion().quests)
                Quests.attemptGiveQuest(Characters.getPlayerCharacter(event.getPlayer()), questId);
        }
    }
}