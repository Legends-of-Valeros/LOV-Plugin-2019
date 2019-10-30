package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.event.GearPickupEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

import java.util.Optional;

public class FetchItemNode extends AbstractQuestNode<Integer> {
    @SerializedName("Item")
    public IInportReference<Integer, IGear> item = IInportValue.ref(this, IGear.class);

    @SerializedName("Count")
    public IInportObject<Integer, Integer> count = IInportValue.of(this, Integer.class, 1);

    @SerializedName("Text")
    public IOutportValue<Integer, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        Optional<IGear> op = item.get(instance);
        String name = op.isPresent() ? op.get().getName() : "<Unknown>";

        int count = this.count.get(instance);
        if(data == Integer.MAX_VALUE)
            return "Found " + (count > 1 ? count + "x" : "") + name;
        return "Find " + (count > 1 ? count + "x" : "") + name;
    });

    @SerializedName("Completed")
    public IOutportTrigger<Integer> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Integer> onActivate = IInportTrigger.of(this, (instance, data) -> {
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

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Integer data, GearPickupEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data == Integer.MAX_VALUE) {
            return;
        }

        Optional<IGear> op = item.get(instance);

        // If the item picked up is not the same as the item we're tracking, ignore it
        if (!op.isPresent() || !op.get().isSimilar(event.getItem())) {
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