package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class DialogOptionNode extends AbstractQuestNode<Void> {
    // TODO: This logic

    @SerializedName("Chosen")
    public IOutportTrigger<Void> onChosen = new IOutportTrigger<>(this);
    
    @SerializedName("Dialog")
    public IInportObject<Void, Object> dialog = IInportValue.of(this, Object.class, null);
    
    @SerializedName("Text")
    public IInportObject<Void, String> text = IInportValue.of(this, String.class, "N/A");
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = IInportTrigger.empty(this);
    
    public DialogOptionNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}