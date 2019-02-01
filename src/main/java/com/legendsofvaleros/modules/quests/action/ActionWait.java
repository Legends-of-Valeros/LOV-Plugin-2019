package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;

public class ActionWait extends AbstractQuestAction {
    int ticks = 1;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        Quests.getInstance().getScheduler().executeInSpigotCircleLater(next::go, ticks);
    }
}