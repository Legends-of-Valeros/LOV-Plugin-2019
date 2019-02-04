package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.bank.TraitBanker;
import com.legendsofvaleros.modules.npcs.trait.bank.repair.TraitBlacksmith;
import com.legendsofvaleros.modules.npcs.trait.bank.trade.TraitTrader;

public class BankIntegration extends Integration {
    public BankIntegration() {
        NPCsController.registerTrait("banker", TraitBanker.class);
        NPCsController.registerTrait("trader", TraitTrader.class);
        NPCsController.registerTrait("blacksmith", TraitBlacksmith.class);
    }
}