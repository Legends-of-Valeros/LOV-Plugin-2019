package com.legendsofvaleros.modules.quests.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;

public class ActionWait extends AbstractQuestAction {
    int ticks = 1;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        QuestController.getInstance().getScheduler().executeInSpigotCircleLater(next::go, ticks);
    }
}