package com.legendsofvaleros.modules.questsold.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.gear.event.RepairItemEvent;
import com.legendsofvaleros.modules.questsold.QuestController;
import com.legendsofvaleros.modules.questsold.action.bank.ActionAddCurrency;
import com.legendsofvaleros.modules.questsold.objective.bank.RepairObjective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BankIntegration extends Integration implements Listener {
    public BankIntegration() {
        QuestController.getInstance().registerEvents(this);

        QuestObjectiveFactory.registerType("repair", RepairObjective.class);
        QuestActionFactory.registerType("currency_give", ActionAddCurrency.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRepairItem(RepairItemEvent event) {
        if (!Characters.isPlayerCharacterLoaded(event.getPlayer())) return;
        QuestController.getInstance().callEvent(event, Characters.getPlayerCharacter(event.getPlayer()));
    }
}