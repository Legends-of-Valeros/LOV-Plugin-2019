package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDeathEvent;
import com.legendsofvaleros.modules.combatengine.events.DamageAddsThreatEvent;
import com.legendsofvaleros.modules.quests.api.QuestEvent;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.QuestInstance;
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
    public void onHurt(QuestInstance instance, Void _, CombatEngineDamageEvent event) {
        if(event.getDamaged().getLivingEntity() != instance.getPlayerCharacter().getPlayer()) {
            return;
        }

        onHurt.run(instance);
    }

    @QuestEvent
    public void onAggro(QuestInstance instance, Void _, DamageAddsThreatEvent event) {
        if(!event.getPossibleTarget().isPlayer() || event.getPossibleTarget().getLivingEntity() != instance.getPlayerCharacter().getPlayer()) {
            return;
        }

        onAggro.run(instance);
    }

    @QuestEvent
    public void onAggro(QuestInstance instance, Void _, CombatEngineDeathEvent event) {
        if(!event.getDied().isPlayer() || event.getDied().getLivingEntity() != instance.getPlayerCharacter().getPlayer()) {
            return;
        }

        onDeath.run(instance);
    }
}