package com.legendsofvaleros.modules.quests.objective.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.regions.Region;
import com.legendsofvaleros.modules.regions.Regions;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class ExitRegionObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
    private String id;
    private String name;

    private transient Region region;

    @Override
    protected void onInit() {
        region = Regions.manager().getRegion(id);

        if (region == null)
            MessageUtil.sendException(Regions.getInstance(), "No regions with that ID in gear. Offender: " + id + " in " + getQuest().getId(), false);
    }

    @Override
    public void onBegin(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        if (region == null) return;

        progress.value = !region.isInside(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return "Leave " + name;
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "You left " + name;
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{RegionEnterEvent.class, RegionLeaveEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        if (event.getClass() == RegionEnterEvent.class) {
            progress.value = false;

        } else if (event.getClass() == RegionLeaveEvent.class) {
            progress.value = true;

        }
    }
}