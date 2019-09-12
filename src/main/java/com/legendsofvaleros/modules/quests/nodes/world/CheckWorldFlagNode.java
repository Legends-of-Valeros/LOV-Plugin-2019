package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class CheckWorldFlagNode extends AbstractQuestNode<Void> {
    @SerializedName("Flag")
    public String flag = "N/A";
    
    @SerializedName("True")
    public IOutportTrigger<Void> onTrue = new IOutportTrigger<>(this);
    
    @SerializedName("False")
    public IOutportTrigger<Void> onFalse = new IOutportTrigger<>(this);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.empty(this);
    
    public CheckWorldFlagNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}