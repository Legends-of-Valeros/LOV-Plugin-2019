package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceEntityNode extends AbstractQuestNode<Void> {
    @SerializedName("Entity")
    public IOutportValue<Void, String> entity = new IOutportValue<>(this, String.class, (instance, data) -> { return null; });
    
    @SerializedName("Reference")
    public IInportValue<Void, String> reference = new IInportValue<>(this, String.class, null);
    
    public ReferenceEntityNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}