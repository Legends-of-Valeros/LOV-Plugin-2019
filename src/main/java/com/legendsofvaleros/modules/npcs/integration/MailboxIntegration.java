package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.trait.mailbox.TraitMailbox;

public class MailboxIntegration extends Integration {
    public MailboxIntegration() {
        NPCsController.getInstance().registerTrait("mailman", TraitMailbox.class);
    }
}
