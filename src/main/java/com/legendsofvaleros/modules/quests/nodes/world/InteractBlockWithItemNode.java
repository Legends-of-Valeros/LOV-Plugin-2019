package com.legendsofvaleros.modules.quests.nodes.world;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.util.Vector;

public class InteractBlockWithItemNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Activate")
    public IInportTrigger<Void> onActivate = new IInportTrigger<>(this, (instance, data) -> { });
    
    @SerializedName("Location")
    public IInportValue<Void, Vector> location = new IInportValue<>(this, Vector.class, null);
    
    @SerializedName("Item")
    public IInportValue<Void, Object> item = new IInportValue<>(this, Object.class, null);
    
    public InteractBlockWithItemNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}