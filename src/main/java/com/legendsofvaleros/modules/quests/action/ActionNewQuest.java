package com.legendsofvaleros.modules.quests.action;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.quests.QuestManager;
import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.modules.quests.quest.stf.IQuest;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;

public class ActionNewQuest extends AbstractQuestAction {
    String questId;

    @Override
    public void play(Player player, Next next) {
        PlayerCharacter pc = Characters.getPlayerCharacter(player);

        ListenableFuture<IQuest> future = QuestManager.getQuest(questId);
        future.addListener(() -> {
            try {
                IQuest quest = future.get();
                if (quest == null)
                    MessageUtil.sendError(player, "Unknown quest: " + questId);
                else
                    Quests.attemptGiveQuest(pc, questId);
            } catch (Exception e) {
                MessageUtil.sendException(Quests.getInstance(), player, e, false);
            }

            next.go();
        }, Quests.getInstance().getScheduler()::async);
    }
}