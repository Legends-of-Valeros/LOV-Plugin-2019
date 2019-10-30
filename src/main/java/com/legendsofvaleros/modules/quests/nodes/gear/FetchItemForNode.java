package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.trait.CitizensTraitLOV;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import java.util.Optional;

public class FetchItemForNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Item")
    public IInportReference<Boolean, IGear> item = IInportValue.ref(this, IGear.class);

    @SerializedName("Count")
    public IInportObject<Boolean, Integer> count = IInportValue.of(this, Integer.class, 1);

    @SerializedName("NPC")
    public IInportReference<Boolean, INPC> npc = IInportValue.ref(this, INPC.class);

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        Optional<IGear> opItem = item.get(instance);
        String itemName = opItem.isPresent() ? opItem.get().getName() : "<Unknown>";

        Optional<INPC> opNPC = npc.get(instance);
        String npcName = opNPC.isPresent() ? opNPC.get().getName() : "<Unknown>";

        int count = this.count.get(instance);
        if(Boolean.TRUE.equals(data))
            return "Retrieved " + (count > 1 ? count + "x" : "") + itemName + " for " + npcName;
        return "Retrieve " + (count > 1 ? count + "x" : "") + itemName + " for " + npcName;
    });

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onComplete = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = IInportTrigger.of(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }

        instance.setNodeInstance(this, false);
    });

    public FetchItemForNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Boolean data, NPCRightClickEvent event) {
        // If we aren't tracking, yet, or we've already completed, ignore it.
        if(data == null || data) {
            return;
        }

        Optional<IGear> opItem = item.get(instance);
        if(!opItem.isPresent()) return;

        Optional<INPC> opNPC = npc.get(instance);
        if(!opNPC.isPresent()) return;

        if (!event.getNPC().hasTrait(CitizensTraitLOV.class)) {
            return;
        }

        CitizensTraitLOV lov = event.getNPC().getTrait(CitizensTraitLOV.class);
        if (lov.getLovNPC() == opNPC.get()) {
            int count = this.count.get(instance);

            if (!ItemUtil.hasItem(instance.getPlayer(), opItem.get(), count)) {
                return;
            }

            Gear.Instance gearInstance = opItem.get().newInstance();
            gearInstance.amount = count;
            ItemUtil.removeItem(instance.getPlayer(), gearInstance);

            instance.setNodeInstance(this, true);

            onComplete.run(instance);
        }
    }
}