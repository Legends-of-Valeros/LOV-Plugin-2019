package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.util.Vector;

public class SoundNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Sound")
    public IInportValue<Void, String> sound = new IInportValue<>(this, String.class, null);
    
    @SerializedName("Volume")
    public IInportValue<Void, Double> volume = new IInportValue<>(this, Double.class, 0D);
    
    @SerializedName("Pitch")
    public IInportValue<Void, Double> pitch = new IInportValue<>(this, Double.class, 0D);
    
    @SerializedName("Location")
    public IInportValue<Void, Vector> location = new IInportValue<>(this, Vector.class, null);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> { });
    
    public SoundNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}