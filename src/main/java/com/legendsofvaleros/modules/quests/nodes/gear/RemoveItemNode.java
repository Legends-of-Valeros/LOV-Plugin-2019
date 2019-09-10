package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class RemoveItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Item")
    public IInportValue<Void, IGear> item = new IInportValue<>(this, IGear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("Completed")
    public IOutportTrigger onCompleted = new IOutportTrigger(this);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        Gear.Instance gearInstance = item.get(instance).newInstance();

        gearInstance.amount = count.get(instance);

        ItemUtil.removeItem(instance.getPlayer(), gearInstance);

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