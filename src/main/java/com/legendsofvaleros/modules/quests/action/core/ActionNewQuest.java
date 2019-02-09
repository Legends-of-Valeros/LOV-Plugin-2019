package com.legendsofvaleros.modules.quests.action.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestController;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.action.AbstractQuestAction;
import com.legendsofvaleros.modules.quests.api.IQuest;
import com.legendsofvaleros.util.MessageUtil;

public class ActionNewQuest extends AbstractQuestAction {
    String questId;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        ListenableFuture<IQuest> future = QuestManager.getQuest(questId);
        future.addListener(() -> {
            try {
                IQuest quest = future.get();
                if (quest == null)
                    MessageUtil.sendError(pc.getPlayer(), "Unknown gear: " + questId);
                else
                    QuestController.attemptGiveQuest(pc, questId);
            } catch (Exception e) {
                MessageUtil.sendException(QuestController.getInstance(), pc.getPlayer(), e);
            }

            next.go();
        }, QuestController.getInstance().getScheduler()::async);
    }
}