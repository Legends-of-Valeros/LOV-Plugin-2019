package com.legendsofvaleros.modules.quests.objective.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class ExitRegionObjective extends AbstractQuestObjective<Boolean> {
    private String id;
    private String name;

    private transient Region region;

    @Override
    protected void onInit() {
        region = RegionController.getManager().getRegion(id);

        if (region == null)
            MessageUtil.sendException(RegionController.getInstance(), "No regions with that ID in quest. Offender: " + id + " in " + getQuest().getId());
    }

    @Override
    public Boolean onBegin(PlayerCharacter pc, Boolean progress) {
        if (region == null) return false;

        return !region.isInside(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
        return progress;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Boolean progress) {
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
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        if (event.getClass() == RegionEnterEvent.class) {
            return false;

        } else if (event.getClass() == RegionLeaveEvent.class) {
            return true;

        }

        return progress;
    }
}