package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceGearNode extends AbstractQuestNode<Void> {
    @SerializedName("Reference")
    public IInportValue<Void, IGear> reference = new IInportValue<>(this, IGear.class, GearController.ERROR_ITEM);

    @SerializedName("Item")
    public IOutportValue<Void, IGear> get = new IOutportValue<>(this, IGear.class,
            (instance, data) -> reference.get(instance));

    public ReferenceGearNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}