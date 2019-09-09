package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;
import com.legendsofvaleros.modules.zones.api.IZone;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;

public class ExitZoneNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Zone")
    public IInportValue<Boolean, IZone> zone = new IInportValue<>(this, IZone.class, null);

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        if(Boolean.TRUE.equals(data))
            return "Exited " + zone.get(instance).getName();
        return "Exit " + zone.get(instance).getName();
    });

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, false);
    });
    
    public ExitZoneNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, ZoneLeaveEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // Fail logic
        if(event.getZone() != zone.get(instance)) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}