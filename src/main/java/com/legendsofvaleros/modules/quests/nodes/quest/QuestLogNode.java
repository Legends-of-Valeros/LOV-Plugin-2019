package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class QuestLogNode extends AbstractQuestNode<Integer> {
    private static final Map<CharacterId, BukkitTask> TIMER = new HashMap<>();

    @SerializedName("OnSuccess")
    public IOutportTrigger<Integer> onSuccess = new IOutportTrigger<>(this);

    @SerializedName("OnFailure")
    public IOutportTrigger<Integer> onFailure = new IOutportTrigger<>(this);

    @SerializedName("Text")
    public IInportValue<Integer, Object> text = new IInportValue<>(this, Object.class, "N/A");

    @SerializedName("Add")
    public IInportTrigger<Integer> onAdd = new IInportTrigger<>(this, (instance, logEntry) -> {
        QuestLogEntry entry = new QuestLogEntry();

        entry.text = text.get(instance).toString();

        if(logEntry == null) {
            logEntry = instance.addLogEntry(entry);
        }

        instance.setNodeInstance(this, logEntry);
    });

    @SerializedName("Success")
    public IInportTrigger<Integer> triggerSuccess = new IInportTrigger<>(this, (instance, logEntry) -> {
        if(logEntry == null) {
            throw new IllegalStateException("Cannot edit a log entry before its been added!");
        }

        QuestLogEntry entry = instance.getLogEntry(logEntry).get();

        if(entry.disabled) {
            throw new IllegalStateException("Cannot edit a disabled log entry!");
        }

        entry.success = true;
        entry.disabled = true;

        this.onSuccess.run(instance);
    });

    @SerializedName("Fail")
    public IInportTrigger<Integer> triggerFail = new IInportTrigger<>(this, (instance, logEntry) -> {
        if(logEntry == null) {
            throw new IllegalStateException("Cannot edit a log entry before its been added!");
        }

        QuestLogEntry entry = instance.getLogEntry(logEntry).get();

        if(entry.disabled) {
            throw new IllegalStateException("Cannot edit a disabled log entry!");
        }

        entry.disabled = true;

        this.onFailure.run(instance);
    });

    @SerializedName("Remove")
    public IInportTrigger<Integer> onRemove = new IInportTrigger<>(this, (instance, logEntry) -> {
        if(logEntry == null) {
            throw new IllegalStateException("Log entry is already removed!");
        }

        instance.removeLogEntry(logEntry);

        instance.setNodeInstance(this, null);
    });

    public QuestLogNode(String id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }
}