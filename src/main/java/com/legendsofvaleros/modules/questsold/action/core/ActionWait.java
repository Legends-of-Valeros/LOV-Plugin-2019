package com.legendsofvaleros.modules.questsold.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.questsold.QuestController;

public class ActionWait extends AbstractQuestAction {
    int ticks = 1;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        QuestController.getInstance().getScheduler().executeInSpigotCircleLater(next::go, ticks);
    }
}