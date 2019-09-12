package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;
import com.legendsofvaleros.modules.zones.api.IZone;

import java.util.Optional;

public class ConquerZoneNode extends AbstractQuestNode<Integer> {
    @SerializedName("Completed")
    public IOutportTrigger<Integer> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Zone")
    public IInportReference<Integer, IZone> zone = IInportValue.ref(this, IZone.class);
    
    @SerializedName("Count")
    public IInportObject<Integer, Integer> count = IInportValue.of(this, Integer.class, 1);

    @SerializedName("Text")
    public IOutportValue<Integer, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        Optional<IZone> op = zone.get(instance);
        String name = op.isPresent() ? op.get().getName() : "<Unknown>";
        if(data == Integer.MAX_VALUE)
            return "Conquered " + name;
        return data + "/" + count.get(instance) + " killed in " + name;
    });

    @SerializedName("Activate")
    public IInportTrigger<Integer> onActivate = IInportTrigger.of(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, 0);
    });
    
    public ConquerZoneNode(String id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Integer data, CombatEngineDeathEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data == Integer.MAX_VALUE) {
            return;
        }

        // Ignore non-quest player
        if(event.getKiller().getLivingEntity() != instance.getPlayer()) {
            return;
        }

        // If they're not within the zone, ignore it
        Optional<IZone> op = zone.get(instance);
        if(!op.isPresent() || !op.get().isInside(event.getKiller().getLivingEntity().getLocation())) {
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