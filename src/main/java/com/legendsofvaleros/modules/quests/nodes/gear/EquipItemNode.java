package com.legendsofvaleros.modules.quests.nodes.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.GearController;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IOutportValue;

public class EquipItemNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Item")
    public IInportValue<Boolean, IGear> item = new IInportValue<>(this, IGear.class, GearController.ERROR_ITEM);

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        if(Boolean.TRUE.equals(data))
            return "Equipped " + item.get(instance).getName();
        return "Equip " + item.get(instance).getName();
    });

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }

        instance.setNodeInstance(this, false);
    });

    public EquipItemNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, ArmorEquipEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // If the item equipped is not the same as the item we're tracking, ignore it
        if (!item.get(instance).isSimilar(event.getNewArmorPiece())) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Boolean data, ItemEquipEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        // If the item equipped is not the same as the item we're tracking, ignore it
        if(!item.get(instance).isSimilar(event.getGear())) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}