package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.api.QuestState;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.events.QuestEndedEvent;

public class QuestEventsNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Quest")
    public IInportValue<Boolean, IQuest> quest = new IInportValue<>(this, IQuest.class,null);

    @SerializedName("OnSuccess")
    public IOutportTrigger onSuccess = new IOutportTrigger(this);

    @SerializedName("Activate")
    public IOutportTrigger onFailure = new IOutportTrigger(this);

    @SerializedName("Activate")
    public IInportTrigger onActivate = new IInportTrigger(this, (instance, data) -> {
        IQuest q = quest.get(instance);
        if(q != null) {
            instance.setNodeInstance(this, false);

            checkQuest(q.getInstance(instance.getPlayerCharacter()));
        }else{
            instance.setNodeInstance(this, false);
        }
    });

    public QuestEventsNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean completed, QuestEndedEvent event) {
        // If it's null, we haven't been activated, yet.
        if(completed == null) return;

        // If it's true, we've already captured the quest completion event.
        if(completed == true) return;

        // If the quest we're tracking is completed
        if(event.getQuest() == quest.get(instance)) {
            checkQuest(event.getInstance());
        }
    }

    private void checkQuest(IQuestInstance instance) {
        QuestState state = instance.getState();

        if(state == QuestState.SUCCESS) {
            onSuccess.run(instance);
            instance.setNodeInstance(this, true);
        }else if(state == QuestState.FAILED){
            onFailure.run(instance);
            instance.setNodeInstance(this, true);
        }
    }
}