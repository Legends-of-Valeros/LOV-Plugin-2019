package com.legendsofvaleros.modules.questsold.core;

import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.questsold.api.IQuest;
import com.legendsofvaleros.modules.questsold.api.IQuestObjective;

import java.lang.ref.WeakReference;
import java.util.Map;

public class QuestUpdater implements Runnable {
    WeakReference<IQuest> quest;
    WeakReference<IQuestObjective> obj;

    int i = 0;

    public QuestUpdater(IQuest quest, IQuestObjective<?> obj) {
        this.quest = new WeakReference<>(quest);
        this.obj = new WeakReference<>(obj);
    }

    public void run() {
        IQuest quest = this.quest.get();
        IQuestObjective obj = this.obj.get();

        if(quest == null || obj == null) return;

        for(Map.Entry<CharacterId, QuestProgressPack> prog : quest.getProgressions()) {
            if(Characters.isPlayerCharacterLoaded(prog.getKey())) {
                PlayerCharacter pc = Characters.getPlayerCharacter(prog.getKey());
                if(!quest.hasProgress(pc)) continue;

                Integer i = quest.getObjectiveGroupI(pc);
                if(i == null) continue;

                if(obj.getGroupIndex() != quest.getObjectiveGroupI(pc)) continue;

                obj.onUpdate(pc, i++);
            }
        }
    }
}