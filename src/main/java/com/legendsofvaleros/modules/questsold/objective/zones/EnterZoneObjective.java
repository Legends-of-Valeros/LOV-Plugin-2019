package com.legendsofvaleros.modules.questsold.objective.zones;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.questsold.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.core.Zone;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class EnterZoneObjective extends AbstractQuestObjective<Boolean> {
    private String id;

    private transient Zone zone;

    @Override
    protected void onInit() {
        zone = ZonesController.getInstance().getZone(id);

        if (zone == null) {
            MessageUtil.sendException(ZonesController.getInstance(), "No zone with that ID in quest. Offender: " + id + " in " + getQuest().getId());
        }
    }

    @Override
    public Boolean onStart(PlayerCharacter pc) {
        if (id == null || zone == null) return false;

        return zone.isInZone(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
        return progress;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Boolean progress) {
        return "Travel to " + (zone == null ? "UNKNOWN" : zone.name);
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "You traveled to " + (zone == null ? "UNKNOWN" : zone.name);
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{ZoneEnterEvent.class, ZoneLeaveEvent.class};
    }

    @Override
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        if (event.getClass() == ZoneEnterEvent.class) {
            return true;

        } else if (event.getClass() == ZoneLeaveEvent.class) {
            return false;

        }

        return progress;
    }
}