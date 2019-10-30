package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.events.DamageAddsThreatEvent;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class CharacterEventsNode extends AbstractQuestNode<Void> {
    @SerializedName("OnHurt")
    public IOutportTrigger<Void> onHurt = new IOutportTrigger<>(this);
    
    @SerializedName("OnAggro")
    public IOutportTrigger<Void> onAggro = new IOutportTrigger<>(this);
    
    @SerializedName("OnDeath")
    public IOutportTrigger<Void> onDeath = new IOutportTrigger<>(this);
    
    public CharacterEventsNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Void _, CombatEngineDamageEvent event) {
        if(event.getDamaged().getLivingEntity() != instance.getPlayer()) {
            return;
        }

        onHurt.run(instance);
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Void _, DamageAddsThreatEvent event) {
        if(!event.getPossibleTarget().isPlayer() || event.getPossibleTarget().getLivingEntity() != instance.getPlayer()) {
            return;
        }

        onAggro.run(instance);
    }

    @QuestEvent
    public void onEvent(IQuestInstance instance, Void _, CombatEngineDeathEvent event) {
        if(!event.getDied().isPlayer() || event.getDied().getLivingEntity() != instance.getPlayer()) {
            return;
        }

        onDeath.run(instance);
    }
}