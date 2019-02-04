package com.legendsofvaleros.modules.quests.objective.zones;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.modules.zones.Zone;
import com.legendsofvaleros.modules.zones.ZonesController;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class ExitZoneObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
    private String id;

    private transient Zone zone;

    @Override
    protected void onInit() {
        zone = ZonesController.manager().getZone(id);

        if (zone == null) {
            MessageUtil.sendException(ZonesController.getInstance(), "No zone with that ID in gear. Offender: " + id + " in " + getQuest().getId(), false);
        }
    }

    @Override
    public void onBegin(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        if (id == null || zone == null) return;

        getProgress(pc).value = !zone.isInZone(pc.getLocation());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return "Leave " + (zone == null ? "UNKNOWN" : zone.name);
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "You left " + (zone == null ? "UNKNOWN" : zone.name);
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{ZoneEnterEvent.class, ZoneLeaveEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        if (event.getClass() == ZoneEnterEvent.class) {
            progress.value = false;

        } else if (event.getClass() == ZoneLeaveEvent.class) {
            progress.value = true;

        }
    }
}