package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;

public class QuestResetNode extends AbstractQuestNode<Void> {
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.of(this, (instance, data) -> {
        QuestController.getInstance().resetQuest(instance.getQuest(), instance.getPlayerCharacter());
    });

    public QuestResetNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}