package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;

public class QuestOfferNode extends AbstractQuestNode<Void> {
    @SerializedName("Quest")
    public IInportValue<IQuest> quest = new IInportValue<>(IQuest.class,this, null);

    @SerializedName("Execute")
    public IInportTrigger onExecute = new IInportTrigger(this, (instance, data) -> {
        IQuest q = quest.get(instance);
        if(q != null)
            QuestController.getInstance().offerQuest(quest.get(instance), instance.getPlayerCharacter());
    });

    public QuestOfferNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}