package com.legendsofvaleros.modules.quests.action;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;
import com.legendsofvaleros.util.Moustache;
import org.bukkit.Bukkit;

public class ActionRunCommand extends AbstractQuestAction {
    String command;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Moustache.translate(pc.getPlayer(), command));

        next.go();
    }
}