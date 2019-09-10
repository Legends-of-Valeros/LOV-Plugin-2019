package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.api.QuestState;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.events.QuestEndedEvent;

public class QuestEventsNode extends AbstractQuestNode<Void> {
    // We don't want to load another quest when this node is loaded, otherwise we could use IQuest instead of String

    @SerializedName("Quest")
    public IInportValue<Void, String> questId = new IInportValue<>(this, String.class,null);

    @SerializedName("OnSuccess")
    public IOutportTrigger onSuccess = new IOutportTrigger(this);

    @SerializedName("OnFailure")
    public IOutportTrigger onFailure = new IOutportTrigger(this);

    public QuestEventsNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Void _, QuestEndedEvent event) {
        // If the quest we're listening to is completed
        if(event.getQuest().getId() == questId.get(instance)) {
            checkQuest(event.getInstance());
        }
    }

    private void checkQuest(IQuestInstance instance) {
        QuestState state = instance.getState();

        if(state == QuestState.SUCCESS) {
            onSuccess.run(instance);
        }else if(state == QuestState.FAILED){
            onFailure.run(instance);
        }
    }
}