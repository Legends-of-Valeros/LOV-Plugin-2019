package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class HasItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Item")
    public IInportValue<Void, IGear> item = new IInportValue<>(this, IGear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("Value")
    public IOutportValue<Void, Boolean> value = new IOutportValue<>(this, Boolean.class, (instance, data) ->
            ItemUtil.hasItem(instance.getPlayer(), item.get(instance), count.get(instance)));

    public HasItemNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}