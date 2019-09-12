package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;
import com.legendsofvaleros.modules.regions.core.IRegion;
import com.legendsofvaleros.modules.regions.event.RegionLeaveEvent;

import java.util.Optional;

public class ExitRegionNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Region")
    public IInportReference<Boolean, IRegion> region = IInportValue.ref(this, IRegion.class);

    @SerializedName("Name")
    public IInportObject<Boolean, String> name = IInportValue.of(this, String.class, "N/A");

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        if(Boolean.TRUE.equals(data))
            return "Exited " + name.get(instance);
        return "Exit " + name.get(instance);
    });

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = IInportTrigger.of(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, false);
    });
    
    public ExitRegionNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Boolean data, RegionLeaveEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // Fail logic
        Optional<IRegion> op = region.get(instance);
        if(!op.isPresent() || event.getRegion() != op.get()) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}