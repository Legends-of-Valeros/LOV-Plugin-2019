package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;

public class SetWorldFlagNode extends AbstractQuestNode<Void> {
    @SerializedName("Flag")
    public String flag = "N/A";
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Value")
    public IInportValue<Void, Boolean> value = new IInportValue<>(this, Boolean.class, false);
    
    public SetWorldFlagNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}