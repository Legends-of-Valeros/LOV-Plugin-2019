package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.util.Vector;

public class ParticleNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Particle")
    public IInportValue<Void, String> particle = new IInportValue<>(this, String.class, null);
    
    @SerializedName("Location")
    public IInportValue<Void, Vector> location = new IInportValue<>(this, Vector.class, null);
    
    @SerializedName("Offset")
    public IInportValue<Void, Vector> offset = new IInportValue<>(this, Vector.class, null);
    
    public ParticleNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}