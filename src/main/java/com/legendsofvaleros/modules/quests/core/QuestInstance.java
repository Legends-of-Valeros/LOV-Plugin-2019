package com.legendsofvaleros.modules.quests.core;

import com.google.gson.annotations.SerializedName;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.api.QuestState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public class QuestInstance implements IQuestInstance {
    @SerializedName("_id")
    private final IQuest quest;

    private transient PlayerCharacter player;

    private QuestState state = QuestState.INACTIVE;

    private QuestLogMap logs;

    private QuestNodeInstanceMap nodes;

    public QuestInstance(PlayerCharacter player, IQuest quest) {
        this.quest = quest;
        this.player = player;

        this.nodes = new QuestNodeInstanceMap();
    }

    public void setPlayer(PlayerCharacter pc) {
        if(this.player != null) throw new IllegalStateException("Player already set!");
        this.player = pc;
    }

    @Override
    public Player getPlayer() {
        return this.player.getPlayer();
    }

    @Override
    public PlayerCharacter getPlayerCharacter() {
        return this.player;
    }

    @Override
    public IQuest getQuest() {
        return this.quest;
    }

    @Override
    public QuestState getState() {
        return state;
    }

    @Override
    public QuestNodeInstanceMap getNodeInstances() {
        return this.nodes;
    }

    @Override
    public <T> void setNodeInstance(IQuestNode<T> node, T instance) {
        nodes.putInstance(node, instance);
    }

    @Override
    public Map<Integer, QuestLogEntry> getLogEntries() {
        if(this.logs == null) this.logs = new QuestLogMap();
        return logs;
    }

    @Override
    public int addLogEntry(QuestLogEntry entry) {
        if(this.logs == null) this.logs = new QuestLogMap();

        return logs.add(entry);
    }

    @Override
    public void updateLogEntry(int id, QuestLogEntry entry) {
        if(this.logs == null) this.logs = new QuestLogMap();

        logs.put(id, entry);

        // Should we fire an updated entry event, here?
    }

    @Override
    public Optional<QuestLogEntry> getLogEntry(int id) {
        return Optional.ofNullable(logs != null ? logs.get(id) : null);
    }

    @Override
    public void removeLogEntry(int id) {
        if(this.logs == null) return;

        logs.remove(id);
        // Should we fire an updated entry event, here?
    }

    @Override
    public <T> T getNodeInstance(IQuestNode<T> node) {
        if(!this.nodes.hasInstance(node))
            this.nodes.putInstance(node, node.newInstance());
        return this.nodes.getInstance(node);
    }

    @Override
    public void reset() {
        if(this.logs != null)
            this.logs.clear();

        this.nodes.clear();
    }

    @Override
    public void setState(QuestState state) {
        if(!Bukkit.getServer().isPrimaryThread())
            throw new IllegalStateException("State can only be set synchronously.");

        if(!this.state.isNextStateAllowed(state)) {
            throw new IllegalStateException("Quest instance cannot be set to '" + state.name() + "' while currently in '" + this.state.name() + "'!");
        }

        this.state = state;

        if(state.isActive()) {
            this.quest.onActivated(this);
        }else{
            this.quest.onDeactivated(this);
        }
    }

    @Override
    public void onActivated() {
        for(IQuestNode node : quest.getNodes())
            node.onActivated(this, this.nodes.getInstance(node));
    }

    @Override
    public void onDeactivated() {
        for(IQuestNode node : quest.getNodes())
            node.onDeactivated(this, this.nodes.getInstance(node));
    }
}
