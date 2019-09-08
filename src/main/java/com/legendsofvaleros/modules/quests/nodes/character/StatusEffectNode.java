package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class StatusEffectNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Apply")
    public IInportTrigger<Void> onApply = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Remove")
    public IInportTrigger<Void> onRemove = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Stats")
    public IInportValue<Void, Object> stats = new IInportValue<>(this, Object.class, null);
    
    @SerializedName("Seconds")
    public IInportValue<Void, Integer> seconds = new IInportValue<>(this, Integer.class, 0);
    
    public StatusEffectNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}