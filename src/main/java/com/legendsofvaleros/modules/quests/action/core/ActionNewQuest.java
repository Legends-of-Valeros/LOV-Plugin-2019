package com.legendsofvaleros.modules.quests.action.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.util.MessageUtil;

public class ActionNewQuest extends AbstractQuestAction {
    String questId;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        QuestController.getInstance().getQuest(questId).onSuccess(val -> {
            if(!val.isPresent()) {
                MessageUtil.sendError(pc.getPlayer(), "Unknown gear: " + questId);
                return;
            }

            QuestController.getInstance().attemptGiveQuest(pc, questId);

            next.go();
        }, QuestController.getInstance().getScheduler()::async);
    }
}