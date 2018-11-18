package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionWait extends AbstractAction {
    int ticks = 1;

    @Override
    public void play(Player player, Next next) {
        Bukkit.getScheduler().runTaskLater(LegendsOfValeros.getInstance(), next::go, ticks);
    }
}