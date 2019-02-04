package com.legendsofvaleros.modules.npcs.integration;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.npcs.NPCs;
import com.legendsofvaleros.modules.npcs.trait.mailbox.TraitMailbox;
import com.legendsofvaleros.modules.skills.gui.recharge.TraitRecharger;

public class MailboxIntegration extends Integration {
    public MailboxIntegration() {
        NPCs.registerTrait("mailman", TraitMailbox.class);
    }
}
