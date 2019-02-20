package com.legendsofvaleros.modules.npcs.trait.mailbox;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.mailbox.MailboxController;
import com.legendsofvaleros.modules.mailbox.gui.MailboxGui;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 01/07/2019
 */
public class TraitMailbox extends LOVTrait {

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        PlayerCharacter playerCharacter = Characters.getPlayerCharacter(player);
        slot.set(new Slot(new ItemBuilder(Material.PAPER).setName("Mailbox").create(), (gui, p, event) -> {
            gui.close(p);
            new MailboxGui(MailboxController.getInstance().getMailbox(playerCharacter.getUniqueCharacterId()).getMails()).open(p);
        }));
    }
}

