package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.auctions.TraitAuctioneer;

public class AuctionsIntegration extends Integration {
    public AuctionsIntegration() {
        NPCsController.getInstance().registerTrait("auctioneer", TraitAuctioneer.class);
    }
}
