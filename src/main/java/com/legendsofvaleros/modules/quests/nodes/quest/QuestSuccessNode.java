package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

import java.util.UUID;

public class QuestSuccessNode extends AbstractQuestNode<Void> {
    @SerializedName("Execute")
    public IInportTrigger onExecute = new IInportTrigger(this, (instance, data) -> {
        QuestController.getInstance().finishQuest(instance.getQuest(), instance.getPlayerCharacter());
    });

    public QuestSuccessNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}