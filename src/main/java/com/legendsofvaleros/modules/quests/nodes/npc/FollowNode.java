package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class FollowNode extends AbstractQuestNode<Void> {
    @SerializedName("OnDied")
    public IOutportTrigger<Void> onOnDied = new IOutportTrigger<>(this);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("NPC")
    public IInportValue<Void, INPC> npc = new IInportValue<>(this, INPC.class, null);
    
    @SerializedName("Reset")
    public IInportTrigger<Void> onReset = new IInportTrigger<>(this, (instance, data) -> { });
    
    public FollowNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}