package com.legendsofvaleros.modules.quests.nodes.npc;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.trait.CitizensTraitLOV;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import java.util.Optional;

public class TalkNode extends AbstractQuestNode<Boolean> {
    @SerializedName("NPC")
    public IInportReference<Boolean, INPC> npc = IInportValue.ref(this, INPC.class);

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        Optional<INPC> op = npc.get(instance);
        String name = op.isPresent() ? op.get().getName() : "<Unknown>";
        if(Boolean.TRUE.equals(data))
            return "Talked to " + name;
        return "Talk to " + name;
    });

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = IInportTrigger.of(this, (instance, data) -> {
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

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Boolean data, NPCRightClickEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        Optional<INPC> op = npc.get(instance);
        if(!op.isPresent()) return;

        if(!event.getNPC().hasTrait(CitizensTraitLOV.class)) return;

        CitizensTraitLOV lov = event.getNPC().getTrait(CitizensTraitLOV.class);

        if(lov.getLovNPC() != op.get()) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}