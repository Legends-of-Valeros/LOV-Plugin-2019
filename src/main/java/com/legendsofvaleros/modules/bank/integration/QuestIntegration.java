package com.legendsofvaleros.modules.bank.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.bank.Bank;

public class QuestIntegration extends Integration {
    public QuestIntegration() {
        Bank.getInstance().getLogger().info("Quest integration class!");
    }
}
