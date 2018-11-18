package com.legendsofvaleros.modules.quests.quest;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.QuestUtil;
import com.legendsofvaleros.modules.quests.objective.stf.IObjective;
import com.legendsofvaleros.modules.quests.quest.stf.QuestStatus;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BasicQuest extends AbstractQuest {
    public BasicQuest(String id) {
        super(id);
    }

    @Override
    public void onTalk(PlayerCharacter pc, QuestStatus status) {
        if (!status.canAccept()) return;

        List<String> list = new ArrayList<>();
        list.add(QuestUtil.moustache(pc.getPlayer(), getDescription()));
        displayBook("New Quest", list, pc.getPlayer(), isForced());

        if (isForced()) {
            onAccept(pc);
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

        IObjective<?>[] group = getCurrentGroup(pc);
        if (group != null)
            for (IObjective<?> obj : group)
                if (!obj.isCompleted(pc))
                    return false;

        return true;
    }

    @Override
    public void checkCompleted(PlayerCharacter pc) {
        if (getProgress(pc).actionI != null)
            return;

        IObjective<?>[] group = getCurrentGroup(pc);
        if (group != null)
            for (IObjective<?> obj : group)
                if (!obj.isCompleted(pc))
                    return;

        startGroup(pc, getCurrentGroupI(pc) + 1);
    }
}