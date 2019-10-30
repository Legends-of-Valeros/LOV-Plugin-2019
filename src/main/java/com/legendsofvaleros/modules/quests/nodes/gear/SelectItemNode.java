package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

public class SelectItemNode extends AbstractQuestNode<IGear> {
    @SerializedName("Items")
    public IInportReference<IGear, IGear[]> items = IInportValue.ref(this, IGear[].class);

    @SerializedName("Selected")
    public IOutportTrigger<IGear> onSelected = new IOutportTrigger<>(this);

    @SerializedName("Item")
    public IOutportValue<IGear, IGear> selectedItem = new IOutportValue<>(this, IGear.class, (instance, data) -> {
        return null;
    });

    @SerializedName("Execute")
    public IInportTrigger<IGear> onExecute = IInportTrigger.empty(this);

    public SelectItemNode(String id) {
        super(id);
    }

    @Override
    public Gear newInstance() {
        return GearController.ERROR_ITEM;
    }
}