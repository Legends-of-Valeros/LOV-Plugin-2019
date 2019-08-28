package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class HasItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Item")
    public IInportValue<Void, Gear> item = new IInportValue<>(this, Gear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("True")
    public IOutportTrigger onTrue = new IOutportTrigger(this);

    @SerializedName("False")
    public IOutportTrigger onFalse = new IOutportTrigger(this);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        // TODO: count up items in inventory

        onFalse.run(instance);
    });

    public HasItemNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}