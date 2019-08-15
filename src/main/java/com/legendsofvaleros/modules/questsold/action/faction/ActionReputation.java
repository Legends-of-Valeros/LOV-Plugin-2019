package com.legendsofvaleros.modules.questsold.action.faction;

import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.factions.FactionController;

public class ActionReputation extends AbstractQuestAction {
    String faction_id;
    int amount;

    @Override
    public void play(PlayerCharacter pc, Next next) {
        FactionController.getInstance().editRep(faction_id, pc, amount);

        next.go();
    }
}