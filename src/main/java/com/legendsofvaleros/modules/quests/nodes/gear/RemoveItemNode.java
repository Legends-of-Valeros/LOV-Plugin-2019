package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class RemoveItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Item")
    public IInportValue<Void, Gear> item = new IInportValue<>(this, Gear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("Completed")
    public IOutportTrigger onCompleted = new IOutportTrigger(this);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        Gear.Instance gear = item.get(instance).newInstance();

        gear.amount = count.get(instance);

        ItemUtil.removeItem(instance.getPlayer(), gear);

        this.onCompleted.run(instance);
    });

    public RemoveItemNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}