package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class KillNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Entity")
    public IInportValue<Void, String> entity = new IInportValue<>(this, String.class, null);
    
    public KillNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}