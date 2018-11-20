package com.legendsofvaleros.modules.zones.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.modules.zones.Zone;
import com.legendsofvaleros.modules.zones.Zones;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class EnterZoneObjective extends AbstractObjective<ObjectiveProgressBoolean> {
    private String id;

    private transient Zone zone;

    @Override
    protected void onInit() {
        zone = Zones.manager().getZone(id);

        if (zone == null) {
            MessageUtil.sendException(Zones.getInstance(), null, new Exception("No zone with that ID in quest. Offender: " + id + " in " + getQuest().getId()), false);
        }
    }

    @Override
    public void onBegin(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        if (id == null || zone == null) return;

        progress.value = zone.isInZone(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return "Travel to " + zone.name;
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "You traveled to " + zone.name;
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{ZoneEnterEvent.class, ZoneLeaveEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        if (event.getClass() == ZoneEnterEvent.class) {
            progress.value = true;

        } else if (event.getClass() == ZoneLeaveEvent.class) {
            progress.value = false;

        }
    }
}