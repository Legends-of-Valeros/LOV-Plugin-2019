package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.regions.core.IRegion;

public class ConquerRegionNode extends AbstractQuestNode<Integer> {
    @SerializedName("Completed")
    public IOutportTrigger<Integer> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Region")
    public IInportValue<Integer, IRegion> region = new IInportValue<>(this, IRegion.class, null);
    
    @SerializedName("Count")
    public IInportValue<Integer, Integer> count = new IInportValue<>(this, Integer.class, 0);
    
    @SerializedName("Activate")
    public IInportTrigger<Integer> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, 0);
    });
    
    public ConquerRegionNode(String id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Integer data, CombatEngineDeathEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data == Integer.MAX_VALUE) {
            return;
        }

        // Ignore non-quest player
        if(event.getKiller().getLivingEntity() != instance.getPlayer()) {
            return;
        }

        // If they're not within the region, ignore it
        if(region.get(instance).isInside(event.getKiller().getLivingEntity().getLocation())) {
            return;
        }

        if(data + 1 < count.get(instance)) {
            instance.setNodeInstance(this, data + 1);
            return;
        }

        // If we've completed the objective, set it to max int value. This makes checking for completion easier
        // than fetching the count every time.
        instance.setNodeInstance(this, Integer.MAX_VALUE);

        onCompleted.run(instance);
    }
}