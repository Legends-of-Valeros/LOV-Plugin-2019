package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class GetWorldFlagNode extends AbstractQuestNode<Void> {
    // TODO: logic

    @SerializedName("Flag")
    public String flag = "N/A";
    
    @SerializedName("Value")
    public IOutportValue<Void, Boolean> value = new IOutportValue<>(this, Boolean.class, (instance, data) -> false);
    
    public GetWorldFlagNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}