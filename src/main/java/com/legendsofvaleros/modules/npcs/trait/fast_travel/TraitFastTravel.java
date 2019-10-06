package com.legendsofvaleros.modules.npcs.trait.fast_travel;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.codingforcookies.robert.slot.Slot;
import com.google.common.util.concurrent.SettableFuture;
import com.legendsofvaleros.modules.bank.core.Money;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.fast_travel.FastTravelController;
import com.legendsofvaleros.modules.npcs.NPCsController;
import com.legendsofvaleros.modules.npcs.api.INPC;
import com.legendsofvaleros.modules.npcs.core.LOVNPC;
import com.legendsofvaleros.modules.npcs.core.NPCEmulator;
import com.legendsofvaleros.modules.npcs.trait.LOVTrait;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.title.Title;
import com.legendsofvaleros.util.title.TitleUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TraitFastTravel extends LOVTrait {
    public static class Entry {
        public INPC npc;
        public int cost;
        public String message;
    }

    public String name;

    public Entry[] destinations;

    @Override
    public void onRightClick(Player player, SettableFuture<Slot> slot) {
        if (!Characters.isPlayerCharacterLoaded(player)) {
            slot.set(null);
            return;
        }

        Collection<String> found = FastTravelController.getInstance().getDiscovered(Characters.getPlayerCharacter(player));

        if (!found.contains(npc_id)) {
            found.add(npc_id);

            FastTravelController.getInstance().addDiscovered(Characters.getPlayerCharacter(player), npc_id);

            Title title = new Title("Discovered Location!", "You can now fast travel here!");
            title.setTitleColor(ChatColor.GREEN);
            title.setSubtitleColor(ChatColor.YELLOW);
            TitleUtil.queueTitle(title, player);
        }

        List<Entry> available = new ArrayList<>();

        for (Entry entry : destinations)
            if (found.contains(entry.npc.getId()))
                available.add(entry);

        if (available.size() == 0) {
            slot.set(null);
            return;
        }

        slot.set(new Slot(new ItemBuilder(Material.LEATHER_BOOTS).setName("Fast Travel").create(), (gui, p, event) -> openGUI(p, available)));
    }

    private void openGUI(Player p, Collection<Entry> available) {
        GUI gui = new GUI(npc.getName());
        gui.type(InventoryType.DISPENSER);

        int i = 0;
        for (Entry entry : available) {
            TraitFastTravel trait = entry.npc.getTrait(TraitFastTravel.class);
            if (trait == null) {
                MessageUtil.sendError(p, "Destination NPC does not have fast travel name set: " + id);
                continue;
            }

            ItemBuilder ib = new ItemBuilder(Material.ENDER_EYE);

            ib.setName(trait.name);
            ib.addLore("", "" + Money.Format.format(entry.cost));

            gui.slot(i++, ib.create(), (gui1, p1, event) -> {
                if (!Money.sub(Characters.getPlayerCharacter(p1), entry.cost)) {
                    NPCEmulator.speak(entry.npc, p1, "You don't have enough crowns for that.");
                    return;
                }

                p1.teleport(entry.npc.getLocation());

                if (entry.message != null && entry.message.length() > 0)
                    NPCEmulator.speak(entry.npc, p1, entry.message);
            });
        }

        gui.open(p);
    }
}