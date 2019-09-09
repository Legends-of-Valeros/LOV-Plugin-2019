package com.legendsofvaleros.modules.quests.nodes.utility;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;

public class TitleNode extends AbstractQuestNode<Void> {
    @SerializedName("Completed")
    public IOutportTrigger<Void> onCompleted = new IOutportTrigger<>(this);
    
    @SerializedName("Title")
    public IInportValue<Void, String> title = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Subtitle")
    public IInportValue<Void, String> subtitle = new IInportValue<>(this, String.class, "N/A");
    
    @SerializedName("Execute")
    public IInportTrigger<Void> onExecute = new IInportTrigger<>(this, (instance, data) -> {
        Title t = new Title(title.get(instance), subtitle.get(instance), 20, 20 * 3, 20);
        t.setTimingsToTicks();
        t.setTitleColor(ChatColor.WHITE);
        t.setSubtitleColor(ChatColor.GRAY);
        TitleUtil.queueTitle(t, instance.getPlayerCharacter().getPlayer());

        onCompleted.run(instance);
    });
    
    public TitleNode(String id) {
        super(id);
    }

    @Override
    public Void newInstance() {
        return null;
    }
}