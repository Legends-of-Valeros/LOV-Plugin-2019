package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.QuestInstance;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class KillNode extends AbstractQuestNode<Boolean> {
    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Entity")
    public IInportValue<Boolean, Mob> entity = new IInportValue<>(this, Mob.class, null);
    
    @SerializedName("Activate")
    public IInportTrigger<Boolean> onActivate = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, false);
    });
    
    public KillNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(QuestInstance instance, Boolean data, CombatEngineDeathEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data) {
            return;
        }

        if(event.getKiller().getLivingEntity() != instance.getPlayerCharacter().getPlayer()) {
            return;
        }

        Mob.Instance mobInstance = Mob.Instance.get(event.getDied().getLivingEntity());
        if(mobInstance == null || mobInstance.mob != entity.get(instance)) {
            return;
        }

        instance.setNodeInstance(this, true);

        onCompleted.run(instance);
    }
}