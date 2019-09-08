package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.regions.core.IRegion;

public class ExitRegionNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Region")
    public IInportValue<Void, IRegion> region = new IInportValue<>(this, IRegion.class, null);
    
    @SerializedName("Name")
    public IInportValue<Void, String> name = new IInportValue<>(this, String.class, "N/A");
    
    public ExitRegionNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}