package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.factions.FactionController;
import com.legendsofvaleros.modules.factions.api.IFaction;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.*;

public class FactionRepNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Faction")
    public IInportReference<Void, IFaction> faction = IInportValue.ref(this, IFaction.class);
    
    @SerializedName("Count")
    public IInportObject<Void, Integer> count = IInportValue.of(this, Integer.class, 0);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.async(this, (instance, data) -> {
        FactionController.getInstance().editReputation(faction.get(instance).orElse(null), instance.getPlayerCharacter(), count.get(instance));

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