package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class ReferenceNPCNode extends AbstractQuestNode<Void> {
    @SerializedName("NPC")
    public IOutportValue<Void, INPC> npc = new IOutportValue<>(this, INPC.class, (instance, data) -> { return null; });
    
    @SerializedName("Reference")
    public IInportValue<Void, INPC> reference = new IInportValue<>(this, INPC.class, null);
    
    public ReferenceNPCNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}