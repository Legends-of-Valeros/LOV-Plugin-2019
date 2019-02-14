package com.legendsofvaleros.modules.quests.core;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.api.IQuestObjective;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BasicQuest extends AbstractQuest {
    @Override
    public void onStart(PlayerCharacter pc) {
        List<String> list = new ArrayList<>();
        list.add(QuestUtil.moustache(pc.getPlayer(), getDescription()));
        displayBook("New Quest", list, pc.getPlayer());

        if (isForced()) {
            onAccept(pc);

            MessageUtil.sendDebugVerbose(pc.getPlayer(), "Quest '" + getId() + "' force accepted!");
        }
    }

    @Override
    public void onCompleted(PlayerCharacter pc) {
        Title title = new Title("Quest Completed", getName(), 10, 40, 10);
        title.setTimingsToTicks();
        title.setTitleColor(ChatColor.GOLD);
        TitleUtil.queueTitle(title, pc.getPlayer());
    }

    @Override
    public boolean isCompleted(PlayerCharacter pc) {
        if (getProgress(pc).actionI != null)
            return false;

        IQuestObjective<?>[] group = getObjectiveGroup(pc);
        if (group != null)
            for (IQuestObjective<?> obj : group)
                if (!obj.isCompleted(pc))
                    return false;

        return true;
    }

    @Override
    public void checkCompleted(PlayerCharacter pc) {
        if (getProgress(pc).actionI != null)
            return;

        IQuestObjective<?>[] group = getObjectiveGroup(pc);
        if (group != null)
            for (IQuestObjective<?> obj : group)
                if (!obj.isCompleted(pc))
                    return;

        Integer i = getObjectiveGroupI(pc);
        startGroup(pc,  (i == null ? 0 : i + 1));
    }
}