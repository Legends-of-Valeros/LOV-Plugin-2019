package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class RandomItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Items")
    public IInportValue<Void, Gear[]> items = new IInportValue<>(this, Gear[].class, new Gear[0]);

    @SerializedName("Item")
    public IOutportValue<Void, Gear> selectedItem = new IOutportValue<>(this, Gear.class, (instance, data) -> {
        Gear[] gears = items.get(instance);

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