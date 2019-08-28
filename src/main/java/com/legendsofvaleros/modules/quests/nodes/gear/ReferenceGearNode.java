package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceGearNode extends AbstractQuestNode<Void> {
    @SerializedName("Reference")
    public IInportValue<Void, Gear> reference = new IInportValue<>(this, Gear.class, GearController.ERROR_ITEM);

    @SerializedName("Item")
    public IOutportValue<Void, Gear> get = new IOutportValue<>(this, Gear.class,
            (instance, data) -> reference.get(instance));

    public ReferenceGearNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}