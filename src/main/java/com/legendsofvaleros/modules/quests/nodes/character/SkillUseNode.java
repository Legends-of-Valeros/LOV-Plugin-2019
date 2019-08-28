package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class SkillUseNode extends AbstractQuestNode<Void> {
    @SerializedName("Text")
    public IInportValue<String> text = new IInportValue<>(String.class, this, "N/A");

    @SerializedName("Completed")
    public IOutportTrigger onComplete = new IOutportTrigger(this);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        instance.getPlayerCharacter().getPlayer().sendMessage(text.get(instance));

        onComplete.run(instance);
    });

    public SkillUseNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}