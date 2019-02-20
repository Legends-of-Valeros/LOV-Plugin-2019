package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.bank.TraitBanker;
import com.legendsofvaleros.modules.npcs.trait.bank.repair.TraitBlacksmith;
import com.legendsofvaleros.modules.npcs.trait.bank.trade.TraitTrader;

public class BankIntegration extends Integration {
    public BankIntegration() {
        NPCsController.getInstance().registerTrait("banker", TraitBanker.class);
        NPCsController.getInstance().registerTrait("trader", TraitTrader.class);
        NPCsController.getInstance().registerTrait("blacksmith", TraitBlacksmith.class);
    }
}