package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.util.Moustache;
import org.bukkit.Bukkit;

public class RunCommandNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Command")
    public IInportValue<Void, String> command = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Moustache.translate(instance.getPlayer(), command.get(instance)));

        onCompleted.run(instance);
    });
    
    public RunCommandNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}