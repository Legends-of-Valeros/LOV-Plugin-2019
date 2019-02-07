package com.legendsofvaleros.modules.quests.objective.bank;

import com.legendsofvaleros.modules.bank.BankController;
import com.legendsofvaleros.modules.npcs.trait.bank.repair.RepairItemEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.item.Gear;
import com.legendsofvaleros.modules.quests.objective.AbstractQuestObjective;
import com.legendsofvaleros.modules.quests.progress.core.QuestObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.event.Event;

public class RepairObjective extends AbstractQuestObjective<QuestObjectiveProgressBoolean> {
    private String id;

    private transient Gear item;

    @Override
    protected void onInit() {
        item = Gear.fromID(id);

        if(item == null)
            MessageUtil.sendException(BankController.getInstance(), "No item with that ID in gear. Offender: " + id + " in " + getQuest().getId());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        return progress.value;
    }

    @Override
    public String getProgressText(PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
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
    public void onEvent(Event event, PlayerCharacter pc, QuestObjectiveProgressBoolean progress) {
        RepairItemEvent e = (RepairItemEvent) event;

        if (id == null || item == null) return;

        if (progress.value) return;

        if (item.isSimilar(e.getItem())) {
            progress.value = true;
        }
    }
}