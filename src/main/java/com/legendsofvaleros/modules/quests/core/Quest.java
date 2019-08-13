package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.modules.quests.api.IQuestInstance;
import com.legendsofvaleros.modules.questsold.api.IQuestPrerequisite;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Quest implements IQuest {

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean isForced() {
        return false;
    }

    @Override
    public List<IQuestPrerequisite> getPrerequisites() {
        return null;
    }

    @Override
    public void getRepeatOptions() {

    }

    @Override
    public Map<UUID, UUID> getConnections() {
        return null;
    }

    @Override
    public void newInstance(PlayerCharacter player) {

    }

    @Override
    public void loadInstance(PlayerCharacter player, QuestInstance instance) {

    }

    @Override
    public Optional<IQuestInstance> getInstance(PlayerCharacter player) {
        return Optional.empty();
    }
}