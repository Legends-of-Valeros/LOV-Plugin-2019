package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class QuestStartedNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Trigger")
    public IOutportTrigger<Boolean> onTriggered = new IOutportTrigger<>(this);

    public QuestStartedNode(String id) {
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