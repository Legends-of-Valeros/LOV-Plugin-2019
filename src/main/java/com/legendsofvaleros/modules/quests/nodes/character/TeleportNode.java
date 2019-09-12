package com.legendsofvaleros.modules.quests.nodes.character;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportObject;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.util.Vector;

public class TeleportNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);

    @SerializedName("Location")
    public IInportObject<Void, Vector> location = IInportValue.of(this, Vector.class, null);
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = IInportTrigger.of(this, (instance, data) -> {
        instance.getPlayer().teleport(location.get(instance).toLocation(instance.getPlayer().getWorld()));

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