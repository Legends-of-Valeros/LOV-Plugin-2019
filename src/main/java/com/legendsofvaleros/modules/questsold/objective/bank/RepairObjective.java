package com.legendsofvaleros.modules.questsold.objective.bank;

import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.event.RepairItemEvent;
import com.legendsofvaleros.modules.questsold.objective.AbstractQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class RepairObjective extends AbstractQuestObjective<Boolean> {
    private String id;

    private transient Gear item;

    @Override
    protected void onInit() {
        item = Gear.fromId(id);

        if(item == null)
            MessageUtil.sendException(BankController.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId());
    }

    @Override
    public Boolean onStart(PlayerCharacter pc) {
        return false;
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, Boolean progress) {
        return progress;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, Boolean progress) {
        return "Repair " + (item == null ? "UNKNOWN" : item.getName());
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Repaired " + (item == null ? "UNKNOWN" : item.getName());
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{RepairItemEvent.class};
    }

    @Override
    public Boolean onEvent(Event event, PlayerCharacter pc, Boolean progress) {
        RepairItemEvent e = (RepairItemEvent) event;

        if (id == null || item == null) return progress;

        if (progress) return true;

        return (item.isSimilar(e.getItem()));
    }
}