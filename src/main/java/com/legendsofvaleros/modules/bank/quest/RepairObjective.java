package com.legendsofvaleros.modules.bank.quest;

import com.legendsofvaleros.LegendsOfValeros;
import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.bank.repair.RepairItemEvent;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.gear.item.GearItem;
import com.legendsofvaleros.modules.quests.objective.stf.AbstractObjective;
import com.legendsofvaleros.modules.quests.progress.ObjectiveProgressBoolean;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.event.Event;

public class RepairObjective extends AbstractObjective<ObjectiveProgressBoolean> {
    private String id;

    private transient GearItem item;

    @Override
    protected void onInit() {
        ListenableFuture<GearItem> future = GearItem.fromID(id);
        future.addListener(() -> {
            try {
                item = future.get();
            } catch (Exception e) {
                MessageUtil.sendException(LegendsOfValeros.getInstance(), null, new Exception("No item with that ID in quest. Offender: " + id + " in " + getQuest().getId()), false);
            }
        }, Utilities.asyncExecutor());
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