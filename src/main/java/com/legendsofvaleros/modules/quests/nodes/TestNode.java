package com.legendsofvaleros.modules.quests.nodes;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.ports.INodeInputTrigger;
import com.legendsofvaleros.modules.quests.core.ports.INodeInputValue;
import com.legendsofvaleros.modules.quests.core.ports.INodeOutputTrigger;
import com.legendsofvaleros.modules.quests.core.ports.INodeOutputValue;
import com.legendsofvaleros.modules.quests.core.AbstractNode;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import org.bukkit.event.EventHandler;

import java.util.Optional;
import java.util.UUID;

public class TestNode extends AbstractNode {
    UUID stage;

    @SerializedName("OutStage")
    public INodeOutputValue<UUID> outStage = new INodeOutputValue<>(this, () -> Optional.ofNullable(stage));

    @SerializedName("Seconds")
    public INodeInputValue<Integer> seconds = new INodeInputValue<>(this);

    @SerializedName("Activate")
    public INodeInputTrigger onActivate = new INodeInputTrigger(this);

    @SerializedName("Completed")
    public INodeOutputTrigger onComplete = new INodeOutputTrigger(this);

    public TestNode(IQuestInstance quest) {
        super(quest);
    }

    @EventHandler
    public void onZoneEnter(ZoneEnterEvent event) {
        Integer i = seconds.get().orElse(0);

        onComplete.run();
    }

    @EventHandler
    public void onZoneLeave(ZoneLeaveEvent event) {

    }
}