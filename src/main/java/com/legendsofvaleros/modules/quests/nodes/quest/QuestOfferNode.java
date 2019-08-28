package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;

public class QuestOfferNode extends AbstractQuestNode<Void> {
    @SerializedName("Execute")
    public IInportTrigger onExecute = new IInportTrigger(this, (instance, data) -> {
        QuestController.getInstance().finishQuest(instance.getQuest(), instance.getPlayerCharacter());
    });

    public QuestOfferNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}