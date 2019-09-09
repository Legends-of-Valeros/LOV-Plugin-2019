package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class DialogNode extends AbstractQuestNode<Void> {
    // TODO: This logic

    @SerializedName("Dialog")
    public IOutportValue<Void, Object> dialog = new IOutportValue<>(this, Object.class, (instance, data) -> { return null; });
    
    @SerializedName("OnActivated")
    public IOutportTrigger<Void> onActivated = new IOutportTrigger<>(this);
    
    @SerializedName("Speaker")
    public IInportValue<Void, INPC> speaker = new IInportValue<>(this, INPC.class, null);
    
    @SerializedName("Text")
    public IInportValue<Void, String> text = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = new IInportTrigger<>(this, (instance, data) -> { });
    
    public DialogNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}