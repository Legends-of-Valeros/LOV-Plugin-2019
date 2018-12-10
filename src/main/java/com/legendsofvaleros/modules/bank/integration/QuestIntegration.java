package com.legendsofvaleros.modules.bank.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.bank.Bank;
import com.legendsofvaleros.modules.bank.quest.ActionAddCurrency;
import com.legendsofvaleros.modules.bank.quest.RepairObjective;
import com.legendsofvaleros.modules.quests.action.stf.ActionFactory;
import com.legendsofvaleros.modules.quests.objective.stf.ObjectiveFactory;

public class QuestIntegration extends Integration {
    public QuestIntegration() {
        Bank.getInstance().getLogger().info("Quest integration class!");
    }
}
