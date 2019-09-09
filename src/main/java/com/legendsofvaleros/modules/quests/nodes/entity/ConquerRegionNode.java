package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.regions.core.IRegion;

public class ConquerRegionNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Region")
    public IInportValue<Boolean, IRegion> region = new IInportValue<>(this, IRegion.class, null);
    
    @SerializedName("Count")
    public IInportValue<Boolean, Integer> count = new IInportValue<>(this, Integer.class, 0);
    
    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, false);
    });
    
    public ConquerRegionNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(QuestInstance instance, Boolean data, SomeEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // Fail logic
        if(!) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}