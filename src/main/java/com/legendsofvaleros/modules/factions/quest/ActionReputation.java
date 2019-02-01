package com.legendsofvaleros.modules.factions.quest;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.factions.FactionModule;
import com.legendsofvaleros.modules.quests.action.stf.AbstractQuestAction;

public class ActionReputation extends AbstractQuestAction {
    String faction_id;
    int amount;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        ListenableFuture<Boolean> future = FactionModule.getInstance().editFactionRep(faction_id, pc, amount);
        future.addListener(next::go, FactionModule.getInstance().getScheduler()::async);
    }
}