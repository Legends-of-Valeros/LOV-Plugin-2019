package com.legendsofvaleros.modules.quests.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.INode;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.ports.INodeInput;
import com.legendsofvaleros.modules.quests.api.ports.INodeOutput;

import java.util.*;

public class QuestInstance implements IQuestInstance {
    final IQuest quest;
    final PlayerCharacter pc;

    final Map<UUID, INode> nodeMap;

    final Multimap<INodeOutput, INodeInput> connectionMap;

    public QuestInstance(IQuest quest, PlayerCharacter pc) {
        this.quest = quest;
        this.pc = pc;

        this.nodeMap = new HashMap<>();
        this.connectionMap = HashMultimap.create();
    }

    @Override
    public IQuest getQuest() {
        return this.quest;
    }

    @Override
    public PlayerCharacter getPlayerCharacter() {
        return this.pc;
    }

    @Override
    public Map<UUID, INode> getNodeMap() {
        return this.nodeMap;
    }

    @Override
    public Optional<INode> getNode(UUID uuid) {
        return Optional.ofNullable(this.nodeMap.get(uuid));
    }
}
