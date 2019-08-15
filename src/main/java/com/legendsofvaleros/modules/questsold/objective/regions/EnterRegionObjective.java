package com.legendsofvaleros.modules.questsold.objective.regions;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.regions.core.Region;
import com.legendsofvaleros.modules.regions.event.RegionEnterEvent;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class EnterRegionObjective extends AbstractQuestObjective<Boolean> {
    private String id;
    private String name;

    private transient Region region;

    @Override
    protected void onInit() {
        region = RegionController.getInstance().getRegion(id);

        if (region == null)
            MessageUtil.sendException(RegionController.getInstance(), "No regions with that ID in quest. Offender: " + id + " in " + getQuest().getId());
    }

    @Override
    public Location getLocation(PlayerCharacter pc) {
        if (region == null) return null;
        int[] center = region.getBounds().getCenter();
        return new Location(pc.getLocation().getWorld(), center[0], center[1], center[2]);
    }

    @Override
    public Boolean onStart(PlayerCharacter pc) {
        if (region == null) return false;

        return region.isInside(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
        return progress;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Boolean progress) {
        return "Go to " + name;
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "You went to " + name;
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{RegionEnterEvent.class, RegionLeaveEvent.class};
    }

    @Override
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        if (event.getClass() == RegionEnterEvent.class) {
            return true;

        } else if (event.getClass() == RegionLeaveEvent.class) {
            return false;

        }

        return progress;
    }
}