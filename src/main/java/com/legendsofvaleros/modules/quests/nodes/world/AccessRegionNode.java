package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.regions.RegionController;
import com.legendsofvaleros.modules.regions.core.IRegion;

public class AccessRegionNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Region")
    public IInportValue<Void, IRegion> region = new IInportValue<>(this, IRegion.class, null);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        RegionController.getInstance().setRegionAccessibility(instance.getPlayerCharacter(), region.get(instance), true);

        onCompleted.run(instance);
    });
    
    public AccessRegionNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}