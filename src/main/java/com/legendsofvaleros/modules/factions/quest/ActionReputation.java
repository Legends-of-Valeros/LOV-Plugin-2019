package com.legendsofvaleros.modules.factions.quest;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.factions.Factions;
import com.legendsofvaleros.modules.quests.action.stf.AbstractAction;
import com.legendsofvaleros.util.Utilities;
import org.bukkit.entity.Player;

public class ActionReputation extends AbstractAction {
    String faction_id;
    int amount;

    @Override
    public void play(Player player, Next next) {
        ListenableFuture<Boolean> future = Factions.getInstance().editFactionRep(faction_id, Characters.getPlayerCharacter(player), amount);
        future.addListener(next::go, Utilities.asyncExecutor());
    }
}