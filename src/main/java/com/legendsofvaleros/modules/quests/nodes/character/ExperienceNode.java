package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;

public class ExperienceNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Count")
    public IInportValue<Void, Integer> count = new IInportValue<>(this, Integer.class, 0);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        instance.getPlayerCharacter().getExperience().addExperience(count.get(instance), false);

        onCompleted.run(instance);
    });
    
    public ExperienceNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}