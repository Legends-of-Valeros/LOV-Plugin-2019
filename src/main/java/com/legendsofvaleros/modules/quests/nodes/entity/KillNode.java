package com.legendsofvaleros.modules.quests.nodes.entity;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.mobs.api.IEntity;
import com.legendsofvaleros.modules.mobs.core.Mob;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

import java.util.Optional;

public class KillNode extends AbstractQuestNode<Integer> {
    @SerializedName("Entity")
    public IInportReference<Integer, IEntity> entity = IInportValue.ref(this, IEntity.class);

    @SerializedName("Count")
    public IInportObject<Integer, Integer> count = IInportValue.of(this, Integer.class, 1);

    @SerializedName("Text")
    public IOutportValue<Integer, String> progressText = new IOutportValue<>(this, String.class, (instance, data) -> {
        Optional<IEntity> op = entity.get(instance);
        String name = op.isPresent() ? op.get().getName() : "<Unknown>";

        int count = this.count.get(instance);
        if(data == Integer.MAX_VALUE)
            return "Killed " + (count > 1 ? count + "x" : "") + name;
        return "Kill " + (count > 1 ? count + "x" : "") + name;
    });

    @SerializedName("Completed")
    public IOutportTrigger<Integer> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Activate")
    public IInportTrigger<Integer> onActivate = IInportTrigger.of(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }
        
        instance.setNodeInstance(this, 0);
    });
    
    public KillNode(String id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }

    @QuestEvent.Async
    public void onEvent(IQuestInstance instance, Integer data, CombatEngineDeathEvent event) {
        // If we aren't tracking, yet, ignore it.
        if(data == null || data == Integer.MAX_VALUE) {
            return;
        }

        if(event.getKiller().getLivingEntity() != instance.getPlayer()) {
            return;
        }

        Optional<IEntity> op = entity.get(instance);
        if(!op.isPresent()) {
            return;
        }

        Mob.Instance mobInstance = Mob.Instance.get(event.getDied().getLivingEntity());
        if(mobInstance == null || mobInstance.entity != op.get()) {
            return;
        }

        if(data + 1 < count.get(instance)) {
            instance.setNodeInstance(this, data + 1);
            return;
        }

        // If we've completed the objective, set it to max int value. This makes checking for completion easier
        // than fetching the count every time.
        instance.setNodeInstance(this, Integer.MAX_VALUE);

        onCompleted.run(instance);
    }
}