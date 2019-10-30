package com.legendsofvaleros.modules.quests.nodes.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.gear.api.IGear;
import com.legendsofvaleros.modules.gear.event.ItemEquipEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

import java.util.Optional;

public class EquipItemNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Item")
    public IInportReference<Boolean, IGear> item = IInportValue.ref(this, IGear.class);

    @SerializedName("Text")
    public IOutportValue<Boolean, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        Optional<IGear> op = item.get(instance);
        String name = op.isPresent() ? op.get().getName() : "<Unknown>";
        if(Boolean.TRUE.equals(data))
            return "Equipped " + name;
        return "Equip " + name;
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

    public EquipItemNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Boolean data, ArmorEquipEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        Optional<IGear> op = item.get(instance);

        // If the item equipped is not the same as the item we're tracking, ignore it
        if (!op.isPresent() || !op.get().isSimilar(event.getNewArmorPiece())) {
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

        Optional<IGear> op = item.get(instance);

        // If the item equipped is not the same as the item we're tracking, ignore it
        if(!op.isPresent() || !op.get().isSimilar(event.getGear())) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}