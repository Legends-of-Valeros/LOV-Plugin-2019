package com.legendsofvaleros.modules.regions.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.modules.regions.Region;
import com.legendsofvaleros.modules.regions.Regions;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class ExitRegionObjective extends AbstractObjective<ObjectiveProgressBoolean> {
    private String id;
    private String name;

    private transient Region region;

    @Override
    protected void onInit() {
        region = Regions.manager().getRegion(id);

        if (region == null)
            MessageUtil.sendException(Regions.getInstance(), null, new Exception("No region with that ID in quest. Offender: " + id + " in " + getQuest().getId()), false);
    }

    @Override
    public void onBegin(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        if (region == null) return;

        progress.value = !region.isInside(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
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
    public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        if (event.getClass() == RegionEnterEvent.class) {
            progress.value = false;

        } else if (event.getClass() == RegionLeaveEvent.class) {
            progress.value = true;

        }
    }
}