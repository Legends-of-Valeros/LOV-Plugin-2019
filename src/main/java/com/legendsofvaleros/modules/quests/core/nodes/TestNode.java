package com.legendsofvaleros.modules.quests.core.nodes;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.INodeInputTrigger;
import com.legendsofvaleros.modules.quests.core.ports.INodeInputValue;
import com.legendsofvaleros.modules.quests.core.ports.INodeOutputTrigger;
import com.legendsofvaleros.modules.quests.core.ports.INodeOutputValue;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import org.bukkit.event.EventHandler;

import java.util.Optional;
import java.util.UUID;

public class TestNode extends AbstractQuestNode<TestNode.Instance> {
    @SerializedName("Stage")
    public INodeInputValue<UUID> stage = new INodeInputValue<>(this);

    @SerializedName("Seconds")
    public INodeInputValue<Integer> seconds = new INodeInputValue<>(this);

    @SerializedName("Activate")
    public INodeInputTrigger onActivate = new INodeInputTrigger(this);

    @SerializedName("Completed")
    public INodeOutputTrigger onComplete = new INodeOutputTrigger(this);

    public TestNode(UUID id) {
        super(id);
    }

    class Instance {
        int count = 0;
    }

    @Override
    public Instance newInstance() {
        return new Instance();
    }

    @QuestEvent
    public void onZoneEnter(IQuestInstance quest, Instance instance, ZoneEnterEvent event) {
        Integer i = seconds.get().orElse(0);

        instance.count++;

        if(instance.count == 1)
            onComplete.run();
    }

    @QuestEvent
    public void onZoneLeave(IQuestInstance quest, Instance instance, ZoneLeaveEvent event) {
        instance.count--;
    }
}