package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.factions.FactionController;
import com.legendsofvaleros.modules.factions.api.IFaction;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class FactionRepNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Faction")
    public IInportValue<Void, IFaction> faction = new IInportValue<>(this, IFaction.class, null);
    
    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 0);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        FactionController.getInstance().editReputation(faction.get(instance), instance.getPlayerCharacter(), count.get(instance));

        onCompleted.run(instance);
    });
    
    public FactionRepNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}