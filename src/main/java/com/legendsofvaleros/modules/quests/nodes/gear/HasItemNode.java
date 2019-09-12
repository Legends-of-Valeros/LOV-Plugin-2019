package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportReference;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

import java.util.Optional;

public class HasItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Item")
    public IInportReference<Void, IGear> item = IInportValue.ref(this, IGear.class);

    @SerializedName("Count")
    public IInportObject<Void, Integer> count = IInportValue.of(this, Integer.class, 1);

    @SerializedName("Value")
    public IOutportValue<Void, Boolean> value = new IOutportValue<>(this, Boolean.class, (instance, data) -> {
        Optional<IGear> op = item.get(instance);
        if(!op.isPresent()) return false;
        return ItemUtil.hasItem(instance.getPlayer(), op.get(), count.get(instance));
    });

    public HasItemNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}