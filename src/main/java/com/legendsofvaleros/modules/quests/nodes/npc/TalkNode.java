package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class TalkNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("NPC")
    public IInportValue<Boolean, INPC> npc = new IInportValue<>(this, INPC.class, null);
    
    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, false);
    });
    
    public TalkNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, NPCRightClickEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        if(!event.getNPC().hasTrait(TraitLOV.class)) return;

        TraitLOV lov = event.getNPC().getTrait(TraitLOV.class);
        if(lov.getNpcData() != npc.get(instance)) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}