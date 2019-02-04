package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.auctions.TraitAuctioneer;

public class AuctionsIntegration extends Integration {
    public AuctionsIntegration() {
        NPCs.registerTrait("auctioneer", TraitAuctioneer.class);
    }
}
