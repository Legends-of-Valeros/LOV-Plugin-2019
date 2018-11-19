package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.Moustache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionRunCommand extends AbstractAction {
    String command;

    @Override
    public void play(Player player, Next next) {
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Moustache.translate(player, command));

        next.go();
    }
}