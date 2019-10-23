package com.legendsofvaleros.modules.parties.integration;

import co.aikar.commands.contexts.OnlinePlayer;
import com.legendsofvaleros.features.gui.core.GUI;
import com.legendsofvaleros.features.gui.item.ItemBuilder;
import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.parties.PartiesController;
import com.legendsofvaleros.modules.parties.commands.PartyCommands;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.features.playermenu.events.PlayerMenuOpenEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

public class PlayerMenuIntegration extends Integration implements Listener {
    @EventHandler
    public void onCharacterMenuOpen(PlayerMenuOpenEvent event) {
        event.addSlot(new ItemBuilder(Material.BEACON).setName("Party").create(), (gui, p, ice) -> {
            final PlayerCharacter pc = Characters.getPlayerCharacter(p);
            final PlayerCharacter clickedPc = Characters.getPlayerCharacter(event.getClicked());

            PlayerParty pParty = PartiesController.getInstance().getPartyByMember(pc.getUniqueCharacterId());
            PlayerParty clickedParty = PartiesController.getInstance().getPartyByMember(clickedPc.getUniqueCharacterId());

            if (clickedParty == null && pParty == null) {
                pc.getPlayer().performCommand("party create");
                pParty = PartiesController.getInstance().getPartyByMember(pc.getUniqueCharacterId());
            }

            boolean openUI = true;
            GUI partyUI = new GUI(clickedPc.getPlayer().getName());

            partyUI.type(InventoryType.HOPPER);

            if (clickedParty == null) {
                partyUI.slot(2, new ItemBuilder(Material.APPLE).setName("Invite to party!").create(), (gui14, p14, event14) -> {
                    PartyCommands.cmdPartyInvite(p14, new OnlinePlayer(clickedPc.getPlayer()));
                    gui14.close(p14);
                });
            } else {
                if (pParty != null) {
                    if (pParty == clickedParty) {
                        if (pParty.getLeader().getPlayerId().compareTo(p.getPlayer().getUniqueId()) == 0) {
                            openUI = false;
                            MessageUtil.sendError(p, "You are not the party leader.");
                        } else {
                            partyUI.slot(2, new ItemBuilder(Material.APPLE).setName("Kick Player").create(), (gui13, p13, event13) -> {
                                PartyCommands.cmdPartyKick(p13, Bukkit.getOfflinePlayer(clickedPc.getPlayer().getUniqueId()));
                                gui13.close(p13);
                            });
                        }
                    } else {
                        openUI = false;
                        MessageUtil.sendError(p, "That player is already in a party!");
                    }
                } else {
                    if (clickedParty.invitations.contains(Characters.getPlayerCharacter(p).getUniqueCharacterId())) {
                        partyUI.slot(2, new ItemBuilder(Material.APPLE).setName("Join their party!").create(), (gui12, p12, event12) -> {
                            PartyCommands.cmdPartyJoin(p12, new OnlinePlayer(clickedPc.getPlayer()));
                            gui12.close(p12);
                        });
                    } else {
                        partyUI.slot(2, new ItemBuilder(Material.APPLE).setName("Request to Join!").create(), (gui1, p1, event1) -> {
                            MessageUtil.sendInfo(clickedPc.getPlayer(), p1.getName() + " is requesting to join your party.");
                            gui1.close(p1);
                        });
                    }
                }
            }

            if (openUI)
                partyUI.open(p);
        });
    }
}
