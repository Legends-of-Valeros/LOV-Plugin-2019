package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;

public class MessageNode extends AbstractQuestNode<Void> {
    @SerializedName("Format")
    public Object format = null;
    
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Text")
    public IInportValue<Void, String> text = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        String line = ChatColor.translateAlternateColorCodes('&', QuestUtil.moustache(instance.getPlayerCharacter(), text.get(instance)));

        switch (format.toString()) {
            case "TEXT":
                instance.getPlayer().sendMessage(line);
                break;
            case "INFO":
                MessageUtil.sendInfo(instance.getPlayer(), line);
                break;
            case "UPDATE":
                MessageUtil.sendUpdate(instance.getPlayer(), line);
                break;
            case "ERROR":
                MessageUtil.sendError(instance.getPlayer(), line);
                break;
        }

        onCompleted.run(instance);
    });
    
    public MessageNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}