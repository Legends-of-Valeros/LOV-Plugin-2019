package com.legendsofvaleros.modules.quests.action;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
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
                    MessageUtil.sendError(pc.getPlayer(), "Unknown quest: " + questId);
                else
                    Quests.attemptGiveQuest(pc, questId);
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), pc.getPlayer(), e, false);
            }

            next.go();
        }, Quests.getInstance().getScheduler()::async);
    }
}