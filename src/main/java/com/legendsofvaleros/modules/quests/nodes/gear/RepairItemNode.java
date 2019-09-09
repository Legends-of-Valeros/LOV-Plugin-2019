package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.event.RepairItemEvent;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class RepairItemNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Item")
    public IInportValue<Boolean, Gear> item = new IInportValue<>(this, Gear.class, GearController.ERROR_ITEM);

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onComplete = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }

        instance.setNodeInstance(this, false);
    });

    public RepairItemNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, RepairItemEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // If the item repaired is not the same as the item we're tracking, ignore it
        if (!item.get(instance).isSimilar(event.getItem())) {
            return;
        }

        instance.setNodeInstance(this, true);

        onComplete.run(instance);
    }
}