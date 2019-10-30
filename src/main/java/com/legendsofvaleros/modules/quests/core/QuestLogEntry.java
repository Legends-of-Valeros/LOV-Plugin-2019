package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.quests.api.IQuestNode;
import com.legendsofvaleros.modules.quests.nodes.quest.QuestLogNode;

import java.util.Optional;

public class QuestLogEntry {
    public String logNodeId;

    public String getText(IQuestInstance instance) {
        Optional<IQuestNode> op = instance.getQuest().getNode(logNodeId);
        if(!op.isPresent()) return "Unable to find Node";
        return ((QuestLogNode)op.get()).text.get(instance);
    }

    public boolean success = false;
    public boolean disabled = false;

    public boolean optional = false;
}