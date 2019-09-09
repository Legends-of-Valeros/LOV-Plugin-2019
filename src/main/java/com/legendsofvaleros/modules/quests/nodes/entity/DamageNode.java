package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.QuestInstance;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class DamageNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Entity")
    public IInportValue<Boolean, String> entity = new IInportValue<>(this, String.class, null);
    
    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, false);
    });
    
    public DamageNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(QuestInstance instance, Boolean data, CombatEngineDamageEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        if(event.getFinalDamage() < 0) {
            return;
        }

        if(event.getAttacker().getLivingEntity() != instance.getPlayerCharacter().getPlayer()) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}