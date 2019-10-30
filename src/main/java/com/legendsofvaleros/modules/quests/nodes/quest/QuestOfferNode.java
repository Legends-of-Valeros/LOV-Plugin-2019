package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;

public class QuestOfferNode extends AbstractQuestNode<Void> {
    // We don't want to load another quest when this node is loaded, otherwise we could use IQuest instead of String

    @SerializedName("Quest")
    public IInportObject<Void, String> questId = IInportValue.of(this, String.class, null);

    @SerializedName("Forced")
    public IInportObject<Void, Boolean> forced = IInportValue.of(this, Boolean.class, false);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.async(this, (instance, data) -> {
        String id = questId.get(instance);
        if(id != null) {
            IQuest quest = QuestController.getInstance().getQuest(id).get();

            if(forced.get(instance))
                QuestController.getInstance().startQuest(quest, instance.getPlayerCharacter());
            else
                QuestController.getInstance().offerQuest(quest, instance.getPlayerCharacter());
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