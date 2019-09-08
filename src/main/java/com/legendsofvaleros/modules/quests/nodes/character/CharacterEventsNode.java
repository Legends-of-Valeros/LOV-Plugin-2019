package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class CharacterEventsNode extends AbstractQuestNode<Void> {
    @SerializedName("OnHurt")
    public IOutportTrigger<Void> onOnHurt = new IOutportTrigger<>(this);
    
    @SerializedName("OnAggro")
    public IOutportTrigger<Void> onOnAggro = new IOutportTrigger<>(this);
    
    @SerializedName("OnDeath")
    public IOutportTrigger<Void> onOnDeath = new IOutportTrigger<>(this);
    
    public CharacterEventsNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}