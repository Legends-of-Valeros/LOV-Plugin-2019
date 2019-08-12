package com.legendsofvaleros.modules.questsold.action;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.questsold.QuestController;
import com.legendsofvaleros.modules.questsold.api.IQuestAction;
import com.legendsofvaleros.modules.questsold.core.QuestProgressPack;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;

public abstract class QuestActionPlay {
    public static ListenableFuture<Boolean> start(Player player, IQuestAction[] questActions) {
        return start(Characters.getPlayerCharacter(player), new QuestProgressPack(0, 0), questActions);
    }

    public static ListenableFuture<Boolean> start(Player player, QuestProgressPack progress, IQuestAction[] questActions) {
        return start(Characters.getPlayerCharacter(player), progress, questActions);
    }

    public static ListenableFuture<Boolean> start(PlayerCharacter pc, QuestProgressPack progress, IQuestAction[] questActions) {
        SettableFuture<Boolean> ret = SettableFuture.create();

        next(pc, progress, questActions, ret);

        return ret;
    }

    private static void next(PlayerCharacter pc, QuestProgressPack progress, IQuestAction[] questActions, SettableFuture<Boolean> future) {
        // If the player logs out or switches characters we should STOP processing actions ASAP.
        if (!pc.isCurrent() || !pc.getPlayer().isOnline()) return;

        if (questActions.length == 0) {
            future.set(false);
            return;
        }

        if (progress.actionI == null) progress.actionI = 0;

        if (progress.actionI >= questActions.length) {
            progress.actionI = null;
            future.set(true);
            return;
        }

        if (questActions[progress.actionI].getClassLock() != null) {
            if (questActions[progress.actionI].getClassLock() != pc.getPlayerClass()) {
                progress.actionI++;

                next(pc, progress, questActions, future);
                return;
            }
        }

        questActions[progress.actionI].play(pc, new IQuestAction.Next() {
            @Override
            public void run(Integer actionI) {
                QuestController.getInstance().getScheduler().executeInSpigotCircle(() -> {
                    if (progress.actionI == null) {
                        MessageUtil.sendException(QuestController.getInstance(), "A weird thing happened");
                        return;
                    }

                    if (actionI == null)
                        progress.actionI++;
                    else
                        progress.actionI = actionI;

                    next(pc, progress, questActions, future);
                });
            }
        });
    }
}