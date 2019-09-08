package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class TitleNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Title")
    public IInportValue<Void, String> title = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Subtitle")
    public IInportValue<Void, String> subtitle = new IInportValue<>(this, String.class, "N/A");
    
    public TitleNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}