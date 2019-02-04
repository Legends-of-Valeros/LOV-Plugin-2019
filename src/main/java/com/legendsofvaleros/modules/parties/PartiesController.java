package com.legendsofvaleros.modules.parties;

import co.aikar.commands.contexts.OnlinePlayer;
import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ModuleListener;
import com.legendsofvaleros.module.annotation.DependsOn;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.skill.Skill;
import com.legendsofvaleros.modules.chat.ChatController;
import com.legendsofvaleros.modules.combatengine.core.CombatEngine;
import com.legendsofvaleros.modules.playermenu.PlayerMenu;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.modules.pvp.PvPCheckEvent;
import com.legendsofvaleros.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;

@DependsOn(CombatEngine.class)
@DependsOn(PlayerMenu.class)
@DependsOn(Characters.class)
@DependsOn(ChatController.class)
public class PartiesController extends ModuleListener {
    private static PartiesController instance;
    public static PartiesController getInstance() { return instance; }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        PartyManager.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new PartyCommands());
    }

    @EventHandler
    public void isPvPAllowed(PvPCheckEvent event) {
        // If PvP is already disabled, parties certainly don't need to enable them!
        if(event.isCancelled()) return;

        if(event.getSkill() != null) {
            // Only disable targetting of bad effects in parties.
            if(event.getSkill().getType() != Skill.Type.HARMFUL) return;
        }

        IParty party = PartyManager.getPartyByMember(Characters.getPlayerCharacter(event.getAttacker()).getUniqueCharacterId());
        if (party != null && party.getMembers().contains(Characters.getPlayerCharacter(event.getDamaged()).getUniqueCharacterId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onCharacterMenuOpen(PlayerMenuOpenEvent event) {
        event.addSlot(new ItemBuilder(Material.BEACON).setName("Party").create(), (gui, p, ice) -> {
            final PlayerCharacter pc = Characters.getPlayerCharacter(p);
            final PlayerCharacter clickedPc = Characters.getPlayerCharacter(event.getClicked());

            PlayerParty pParty = (PlayerParty) PartyManager.getPartyByMember(pc.getUniqueCharacterId());
            PlayerParty clickedParty = (PlayerParty) PartyManager.getPartyByMember(clickedPc.getUniqueCharacterId());

            if (clickedParty == null && pParty == null) {
                pc.getPlayer().performCommand("party create");
                pParty = (PlayerParty) PartyManager.getPartyByMember(pc.getUniqueCharacterId());
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

    public void onChat(Player p, BaseComponent[] bc) {
        PlayerParty party = (PlayerParty) PartyManager.getPartyByMember(Characters.getPlayerCharacter(p).getUniqueCharacterId());
        if (party == null) {
            MessageUtil.sendError(p, "You are not in a party.");
            return;
        }

        for (Player pl : party.getOnlineMembers()) {
            pl.spigot().sendMessage(bc);
        }
    }

}