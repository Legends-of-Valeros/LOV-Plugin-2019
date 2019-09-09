package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class FetchItemNode extends AbstractQuestNode<Integer> {
    @SerializedName("Item")
    public IInportValue<Integer, IGear> item = new IInportValue<>(this, IGear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Integer, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("Text")
    public IOutportValue<Integer, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        int count = this.count.get(instance);
        if(data == Integer.MAX_VALUE)
            return "Found " + (count > 1 ? count + "x" : "") + item.get(instance).getName();
        return "Find " + (count > 1 ? count + "x" : "") + item.get(instance).getName();
    });

    @SerializedName("Completed")
    public IOutportTrigger<Integer> onCompleted = new IOutportTrigger<>(this);

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
        if(data == null || data == Integer.MAX_VALUE) {
            return;
        }

        // If the item picked up is not the same as the item we're tracking, ignore it
        if (!item.get(instance).isSimilar(event.getItem())) {
            return;
        }

        if(data + 1 < count.get(instance)) {
            instance.setNodeInstance(this, data + 1);
            return;
        }

        // If we've completed the objective, set it to max int value. This makes checking for completion easier
        // than fetching the count every time.
        instance.setNodeInstance(this, Integer.MAX_VALUE);

        onCompleted.run(instance);
    }
}