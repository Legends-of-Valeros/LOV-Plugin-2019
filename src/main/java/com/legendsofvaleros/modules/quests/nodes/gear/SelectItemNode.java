package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class SelectItemNode extends AbstractQuestNode<Gear> {
    @SerializedName("Items")
    public IInportValue<Gear, Gear[]> items = new IInportValue<>(this, Gear[].class, new Gear[0]);

    @SerializedName("Selected")
    public IOutportTrigger<Gear> onSelected = new IOutportTrigger<>(this);

    @SerializedName("Item")
    public IOutportValue<Gear, Gear> selectedItem = new IOutportValue<>(this, Gear.class, (instance, data) -> {
        return null;
    });

    @SerializedName("Execute")
    public IInportTrigger<Gear> onExecute = new IInportTrigger<>(this, (instance, data) -> {
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