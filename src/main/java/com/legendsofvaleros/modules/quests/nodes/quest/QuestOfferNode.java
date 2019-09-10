package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;

public class QuestOfferNode extends AbstractQuestNode<Void> {
    // We don't want to load another quest when this node is loaded, otherwise we could use IQuest instead of String

    @SerializedName("Quest")
    public IInportValue<Void, String> questId = new IInportValue<>(this, String.class, null);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        String id = questId.get(instance);
        if(id != null) {
            QuestController.getInstance().getQuest(id).onSuccess(quest -> {
                if(!quest.isPresent()) return;

                QuestController.getInstance().offerQuest(quest.get(), instance.getPlayerCharacter());
            });
        }
    });

    public QuestOfferNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}