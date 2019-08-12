package com.legendsofvaleros.modules.quests.nodes;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.ports.INodeInputTrigger;
import com.legendsofvaleros.modules.quests.api.ports.INodeInputValue;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutputTrigger;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutputValue;
import com.legendsofvaleros.modules.quests.core.AbstractNode;
import com.legendsofvaleros.modules.zones.event.ZoneEnterEvent;
import com.legendsofvaleros.modules.zones.event.ZoneLeaveEvent;
import org.bukkit.event.EventHandler;

public class TestNode extends AbstractNode<Boolean> {
    @SerializedName("Stage")
    public INodeInputValue<Object> stage = new INodeInputValue<>();

    @SerializedName("OutStage")
    public INodeOutputValue<Object> outStage = new INodeOutputValue<>(quest -> {
        return quest.getData(null);
    });

    @SerializedName("Seconds")
    public INodeInputValue<Integer> seconds = new INodeInputValue<>();

    @SerializedName("Activate")
    public INodeInputTrigger onActivate = new INodeInputTrigger((quest) -> {
        stage.get(quest);
        quest.setData(this, true);
    });

    @SerializedName("Completed")
    public INodeOutputTrigger complete = new INodeOutputTrigger((quest) ->
        quest.setData(this, false)
    );

    @EventHandler
    public void onZoneEnter(IQuestInstance quest, ZoneEnterEvent event) {
        Integer i = seconds.get(quest).orElse(0);

        complete.run(quest);
    }

    @EventHandler
    public void onZoneLeave(IQuestInstance quest, ZoneLeaveEvent event) {

    }
}