package com.legendsofvaleros.modules.quests.nodes;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestLogNode extends AbstractQuestNode<Integer> {
    private static final Map<CharacterId, BukkitTask> TIMER = new HashMap<>();

    @SerializedName("Text")
    public IInportValue<Object> text = new IInportValue<>(Object.class, this, "N/A");

    @SerializedName("Strike")
    public IInportValue<Boolean> strikethrough = new IInportValue<>(Boolean.class, this, false);

    @SerializedName("Update")
    public IInportTrigger<Integer> onUpdate = new IInportTrigger<>(this, (instance, logEntry) -> {
        if(logEntry == null)
            logEntry = instance.addLogEntry(new QuestLogEntry("N/A", false));

        QuestLogEntry entry = instance.getLogEntry(logEntry).get();
        entry.text = text.get(instance).toString();
        entry.strikethrough = strikethrough.get(instance);

        instance.setNodeInstance(this, logEntry);
    });

    @SerializedName("Remove")
    public IInportTrigger<Integer> onRemove = new IInportTrigger<>(this, (instance, logEntry) -> {
        if(logEntry == null)
            throw new IllegalStateException("Log entry is already removed!");

        instance.removeLogEntry(logEntry);

        instance.setNodeInstance(this, null);
    });

    public QuestLogNode(UUID id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }
}