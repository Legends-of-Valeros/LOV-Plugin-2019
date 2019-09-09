package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.scheduler.InternalTask;

import java.util.HashMap;
import java.util.Map;

public class WaitTicksNode extends AbstractQuestNode<Boolean> {
    Map<CharacterId, InternalTask> waiting = new HashMap<>();

    @SerializedName("Completed")
    public IOutportTrigger<Boolean> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Seconds")
    public IInportValue<Boolean, Double> seconds = new IInportValue<>(this, Double.class, 0D);
    
    @SerializedName("Execute")
    public IInportTrigger<Boolean> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        // If it's not null, then this node has already been activated.
        if(data != null) {
            return;
        }

        this.onActivated(instance, instance.getNodeInstance(this));
    });
    
    public WaitTicksNode(String id) {
        super(id);
    }

    @Override
    public Boolean newInstance() {
        return null;
    }

    @Override
    public void onActivated(IQuestInstance instance, Boolean data) {
        // If we aren't in the credits screen, yet, ignore it.
        if(data == null) {
            return;
        }

        InternalTask it = QuestController.getInstance().getScheduler().executeInMyCircleLater(() -> {
            waiting.remove(instance.getPlayerCharacter().getUniqueCharacterId());

            instance.setNodeInstance(this, true);

            onCompleted.run(instance);
        }, (long)(seconds.get(instance) * 20));

        it = waiting.put(instance.getPlayerCharacter().getUniqueCharacterId(), it);

        if(it != null) {
            it.cancel();
        }

        instance.setNodeInstance(this, false);
    }

    @Override
    public void onDeactivated(IQuestInstance instance, Boolean data) {
        // Remove any players currently in the credits
        InternalTask it = waiting.remove(instance.getPlayerCharacter().getPlayer().getUniqueId());

        if(it != null) {
            it.cancel();
        }
    }
}