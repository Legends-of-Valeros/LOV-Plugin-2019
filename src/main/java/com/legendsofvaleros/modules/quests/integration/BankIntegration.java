package com.legendsofvaleros.modules.quests.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.quests.action.QuestActionFactory;
import com.legendsofvaleros.modules.quests.objective.bank.ActionAddCurrency;
import com.legendsofvaleros.modules.quests.objective.bank.RepairObjective;
import com.legendsofvaleros.modules.quests.objective.QuestObjectiveFactory;

public class BankIntegration extends Integration {
    public BankIntegration() {
        QuestObjectiveFactory.registerType("repair", RepairObjective.class);
        QuestActionFactory.registerType("currency_give", ActionAddCurrency.class);
    }
}