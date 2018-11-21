package com.legendsofvaleros.modules.bank.quest;

import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.repair.RepairItemEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class RepairObjective extends AbstractObjective<ObjectiveProgressBoolean> {
    private String id;

    private transient GearItem item;

    @Override
    protected void onInit() {
        item = GearItem.fromID(id);

        if(item == null)
            MessageUtil.sendException(Bank.getInstance(), "No item with that ID in quest. Offender: " + id + " in " + getQuest().getId(), false);
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        return "Repair " + item.getName();
    }

    @Override
    public String getCompletedText(PlayerCharacter pc) {
        return "Repair " + item.getName();
    }

    @Override
    public Class<? extends Event>[] getRequestedEvents() {
        return new Class[]{RepairItemEvent.class};
    }

    @Override
    public void onEvent(Event event, PlayerCharacter pc, ObjectiveProgressBoolean progress) {
        RepairItemEvent e = (RepairItemEvent) event;

        if (id == null || item == null) return;

        if (progress.value) return;

        if (item.isSimilar(e.getItem())) {
            progress.value = true;
        }
    }
}