package com.legendsofvaleros.modules.npcs.trait.queue;

import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.modules.queue.gui.QueueGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 11/25/2018
 */
public class TraitQueue extends LOVTrait {

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.BOOK).setName("Queue").create(), (gui, p, event) -> {
            gui.close(p);
            new QueueGui().open(p);
        }));
    }
}
