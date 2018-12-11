package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.Quests;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import org.bukkit.entity.Player;

public class ActionWait extends AbstractQuestAction {
    int ticks = 1;

    @Override
    public void play(Player player, Next next) {
        Quests.getInstance().getScheduler().executeInSpigotCircleLater(next::go, ticks);
    }
}