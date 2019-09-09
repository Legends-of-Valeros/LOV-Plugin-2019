package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class SelectItemNode extends AbstractQuestNode<IGear> {
    @SerializedName("Items")
    public IInportValue<IGear, IGear[]> items = new IInportValue<>(this, IGear[].class, new Gear[0]);

    @SerializedName("Selected")
    public IOutportTrigger<IGear> onSelected = new IOutportTrigger<>(this);

    @SerializedName("Item")
    public IOutportValue<IGear, IGear> selectedItem = new IOutportValue<>(this, IGear.class, (instance, data) -> {
        return null;
    });

    @SerializedName("Execute")
    public IInportTrigger<IGear> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        // TODO: create selection GUI

        onSelected.run(instance);
    });

    public SelectItemNode(String id) {
        super(id);
    }

    @Override
    public Gear newInstance() {
        return GearController.ERROR_ITEM;
    }
}