package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.regions.core.IRegion;

public class ConquerRegionNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Region")
    public IInportValue<Void, IRegion> region = new IInportValue<>(this, IRegion.class, null);
    
    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 0);
    
    public ConquerRegionNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}