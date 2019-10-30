package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

public class DialogNode extends AbstractQuestNode<Void> {
    // TODO: This logic

    @SerializedName("Dialog")
    public IOutportValue<Void, Object> dialog = new IOutportValue<>(this, Object.class, (instance, data) -> { return null; });
    
    @SerializedName("OnActivated")
    public IOutportTrigger<Void> onActivated = new IOutportTrigger<>(this);
    
    @SerializedName("Speaker")
    public IInportObject<Void, INPC> speaker = IInportValue.of(this, INPC.class, null);
    
    @SerializedName("Text")
    public IInportObject<Void, String> text = IInportValue.of(this, String.class, "N/A");
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate =IInportTrigger.empty(this);
    
    public DialogNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}