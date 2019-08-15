package com.legendsofvaleros.modules.quests.nodes;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.INodeOutputTrigger;

import java.util.UUID;

public class StartedNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Triggered")
    public INodeOutputTrigger onTriggered = new INodeOutputTrigger(this);

    public StartedNode(UUID id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return false;
    }

    @Override
    public void onActivated(IQuestInstance instance, Boolean started) {
        if(!started) {
            instance.setNodeInstance(this, true);

            onTriggered.run(instance);
        }
    }
}
