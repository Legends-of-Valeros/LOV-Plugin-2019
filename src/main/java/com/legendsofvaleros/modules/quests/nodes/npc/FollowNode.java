package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportReference;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class FollowNode extends AbstractQuestNode<Void> {
    // TODO: This logic

    @SerializedName("OnDied")
    public IOutportTrigger<Void> onDied = new IOutportTrigger<>(this);
    
    @SerializedName("NPC")
    public IInportReference<Void, INPC> npc = IInportValue.ref(this, INPC.class);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.empty(this);
    
    @SerializedName("Reset")
    public IInportTrigger<Void> onReset = IInportTrigger.empty(this);
    
    public FollowNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}