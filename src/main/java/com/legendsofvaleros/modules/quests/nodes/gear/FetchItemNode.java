package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class FetchItemNode extends AbstractQuestNode<Integer> {
    @SerializedName("Item")
    public IInportValue<Integer, Gear> item = new IInportValue<>(this, Gear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Integer, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("Completed")
    public IOutportTrigger<Integer> onComplete = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Integer> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }

        instance.setNodeInstance(this, 0);
    });

    public FetchItemNode(String id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Integer data, GearPickupEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null) {
            return;
        }

        int count = this.count.get(instance);

        // If we've already obtained all the items, ignore it
        if(data >= count) {
            return;
        }

        // If the item picked up is not the same as the item we're tracking, ignore it
        if (!item.get(instance).isSimilar(event.getItem())) {
            return;
        }

        instance.setNodeInstance(this, data + 1);

        // If the new count should trigger completion
        if(data >= count) {
            onComplete.run(instance);
        }
    }
}