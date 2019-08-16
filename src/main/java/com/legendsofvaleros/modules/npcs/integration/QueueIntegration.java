package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.queue.TraitQueue;

/**
 * Created by Crystall on 08/04/2019
 */
public class QueueIntegration extends Integration {
    public QueueIntegration() {
        NPCsController.getInstance().registerTrait("traitqueue", TraitQueue.class);
    }
}
