package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class DialogOptionNode extends AbstractQuestNode<Void> {
    @SerializedName("Chosen")
    public IOutportTrigger<Void> onChosen = new IOutportTrigger<>(this);
    
    @SerializedName("Dialog")
    public IInportValue<Void, Object> dialog = new IInportValue<>(this, Object.class, null);
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Text")
    public IInportValue<Void, String> text = new IInportValue<>(this, String.class, "N/A");
    
    public DialogOptionNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}