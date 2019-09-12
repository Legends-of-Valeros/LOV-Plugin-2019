package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

import java.util.Optional;

public class RemoveItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Item")
    public IInportReference<Void, IGear> item = IInportValue.ref(this, IGear.class);

    @SerializedName("Count")
    public IInportObject<Void, Integer> count = IInportValue.of(this, Integer.class, 1);

    @SerializedName("Completed")
    public IOutportTrigger onCompleted = new IOutportTrigger(this);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.async(this, (instance, data) -> {
        Optional<IGear> op = item.get(instance);
        if(op.isPresent()) {
            Gear.Instance gearInstance = op.get().newInstance();

            gearInstance.amount = count.get(instance);

            ItemUtil.removeItem(instance.getPlayer(), gearInstance);
        }

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