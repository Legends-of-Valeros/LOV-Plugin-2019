package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class RandomItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Items")
    public IInportObject<Void, IGear[]> items = IInportValue.of(this, IGear[].class, new IGear[0]);

    @SerializedName("Item")
    public IOutportValue<Void, IGear> selectedItem = new IOutportValue<>(this, IGear.class, (instance, data) -> {
        IGear[] gears = items.get(instance);

        return gears[(int)Math.floor(Math.random() * gears.length)];
    });

    public RandomItemNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}