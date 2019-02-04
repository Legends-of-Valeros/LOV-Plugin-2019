package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.bank.TraitBanker;
import com.legendsofvaleros.modules.npcs.trait.bank.repair.TraitBlacksmith;
import com.legendsofvaleros.modules.npcs.trait.bank.trade.TraitTrader;

public class BankIntegration extends Integration {
    public BankIntegration() {
        NPCs.registerTrait("banker", TraitBanker.class);
        NPCs.registerTrait("trader", TraitTrader.class);
        NPCs.registerTrait("blacksmith", TraitBlacksmith.class);
    }
}
