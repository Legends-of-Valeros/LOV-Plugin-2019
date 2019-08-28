package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class WaitNode extends AbstractQuestNode<Void> {
    @SerializedName("Text")
    public IInportValue<Void, String> text = new IInportValue<>(this, String.class, "N/A");

    @SerializedName("Completed")
    public IOutportTrigger onComplete = new IOutportTrigger(this);

    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        instance.getPlayerCharacter().getPlayer().sendMessage(text.get(instance));

        onComplete.run(instance);
    });

    public WaitNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}