package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class TeleportNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Entity")
    public IInportValue<Void, LivingEntity> entity = new IInportValue<>(this, LivingEntity.class, null);
    
    @SerializedName("Location")
    public IInportValue<Void, Vector> location = new IInportValue<>(this, Vector.class, null);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        entity.get(instance).teleport(location.get(instance).toLocation(entity.get(instance).getWorld()));

        onCompleted.run(instance);
    });
    
    public TeleportNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}