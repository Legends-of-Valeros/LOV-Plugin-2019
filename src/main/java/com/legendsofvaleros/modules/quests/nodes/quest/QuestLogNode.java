package com.legendsofvaleros.modules.quests.nodes.quest;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.quests.core.AbstractQuestNode;
import com.legendsofvaleros.modules.quests.core.QuestLogEntry;
import com.legendsofvaleros.modules.quests.core.ports.IInportTrigger;
import com.legendsofvaleros.modules.quests.core.ports.IInportValue;
import com.legendsofvaleros.modules.quests.core.ports.IOutportTrigger;
import com.legendsofvaleros.modules.quests.events.QuestLogEntryAddedEvent;
import com.legendsofvaleros.modules.quests.events.QuestLogEntryRemovedEvent;
import com.legendsofvaleros.modules.quests.events.QuestLogEntryUpdatedEvent;
import org.bukkit.Bukkit;

public class QuestLogNode extends AbstractQuestNode<Integer> {
    @SerializedName("OnSuccess")
    public IOutportTrigger<Integer> onSuccess = new IOutportTrigger<>(this);

    @SerializedName("OnFailure")
    public IOutportTrigger<Integer> onFailure = new IOutportTrigger<>(this);

    @SerializedName("Text")
    public IInportValue<Integer, String> text = new IInportValue<>(this, String.class, "N/A");

    @SerializedName("Optional")
    public IInportValue<Integer, Boolean> optional = new IInportValue<>(this, Boolean.class, false);

    @SerializedName("Add")
    public IInportTrigger<Integer> onAdd = new IInportTrigger<>(this, (instance, logEntry) -> {
        QuestLogEntry entry = new QuestLogEntry();

        entry.logNodeId = getId();
        entry.optional = optional.get(instance);

        if(logEntry == null) {
            logEntry = instance.addLogEntry(entry);
        }

        instance.setNodeInstance(this, logEntry);

        Bukkit.getPluginManager().callEvent(new QuestLogEntryAddedEvent(instance, entry));
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

        Bukkit.getPluginManager().callEvent(new QuestLogEntryUpdatedEvent(instance, entry, true));

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

        Bukkit.getPluginManager().callEvent(new QuestLogEntryUpdatedEvent(instance, entry, false));

        this.onFailure.run(instance);
    });

    @SerializedName("Remove")
    public IInportTrigger<Integer> onRemove = new IInportTrigger<>(this, (instance, logEntry) -> {
        if(logEntry == null) {
            throw new IllegalStateException("Log entry is already removed!");
        }

        QuestLogEntry entry = instance.getLogEntry(logEntry).get();

        instance.removeLogEntry(logEntry);

        instance.setNodeInstance(this, null);

        Bukkit.getPluginManager().callEvent(new QuestLogEntryRemovedEvent(instance, entry));
    });

    public QuestLogNode(String id) {
        super(id);
    }

    @Override
    public Integer newInstance() {
        return null;
    }
}