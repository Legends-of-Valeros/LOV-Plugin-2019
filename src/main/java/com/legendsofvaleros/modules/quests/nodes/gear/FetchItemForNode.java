package com.legendsofvaleros.modules.quests.nodes.gear;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.core.Gear;
import com.legendsofvaleros.modules.gear.core.ItemUtil;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.trait.TraitLOV;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class FetchItemForNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Item")
    public IInportValue<Boolean, INPC> npc = new IInportValue<>(this, INPC.class, null);

    @SerializedName("Item")
    public IInportValue<Boolean, Gear> item = new IInportValue<>(this, Gear.class, GearController.ERROR_ITEM);

    @SerializedName("Count")
    public IInportValue<Boolean, Integer> count = new IInportValue<>(this, Integer.class, 1);

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onComplete = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
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

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, NPCRightClickEvent event) {
        // If we aren't tracking, yet, or we've already completed, ignore it.
        if(data == null || data) {
            return;
        }

        if (!event.getNPC().hasTrait(TraitLOV.class)) {
            return;
        }

        TraitLOV lov = event.getNPC().getTrait(TraitLOV.class);
        if (lov.getNpcData() == npc.get(instance)) {
            Gear item = this.item.get(instance);
            int count = this.count.get(instance);

            if (!ItemUtil.hasItem(instance.getPlayer(), item, count)) {
                return;
            }

            Gear.Instance gearInstance = item.newInstance();
            gearInstance.amount = count;
            ItemUtil.removeItem(instance.getPlayer(), gearInstance);

            instance.setNodeInstance(this, true);

            onComplete.run(instance);
        }
    }
}