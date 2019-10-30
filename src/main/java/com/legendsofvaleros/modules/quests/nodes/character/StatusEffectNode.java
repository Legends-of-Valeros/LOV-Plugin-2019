package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class StatusEffectNode extends AbstractQuestNode<Void> {
    // TODO: Not implemented, yet, as we don't have a system for defining effects.

    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Stats")
    public IInportObject<Void, Object> stats = IInportValue.of(this, Object.class, null);
    
    @SerializedName("Seconds")
    public IInportObject<Void, Integer> seconds = IInportValue.of(this, Integer.class, 0);
    
    @SerializedName("Apply")
    public IInportTrigger<Void> onApply = IInportTrigger.of(this, (instance, data) -> {

    });
    
    @SerializedName("Remove")
    public IInportTrigger<Void> onRemove =IInportTrigger.empty(this);
    
    public StatusEffectNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}