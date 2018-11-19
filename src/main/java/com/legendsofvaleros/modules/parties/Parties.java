package com.legendsofvaleros.modules.parties;

import com.codingforcookies.robert.core.GUI;
import com.codingforcookies.robert.item.ItemBuilder;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.ListenerModule;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.chat.Chat;
import com.legendsofvaleros.modules.chat.IChannelHandler;
import com.legendsofvaleros.modules.combatengine.events.CombatEngineDamageEvent;
import com.legendsofvaleros.modules.playermenu.PlayerMenuOpenEvent;
import com.legendsofvaleros.util.MessageUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;

public class Parties extends ListenerModule {
    private static Parties plugin;
    public static Parties getInstance() {
        return plugin;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        plugin = this;

        PartyManager.onEnable();

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new PartyCommands());

        //TODO move into chatchannel enum
        Chat.getInstance().registerChannel('P', new IChannelHandler() {
            @Override public ChatColor getTagColor() {
                return ChatColor.YELLOW;
            }

            @Override public ChatColor getChatColor() {
                return ChatColor.YELLOW;
            }

            @Override public String getName(Player p) {
                return "Party";
            }

            @Override public boolean canSetDefault() {
                return true;
            }

            @Override public boolean canDisable() {
                return false;
            }

            @Override
            public void onChat(Player p, FancyMessage fm) {
                PlayerParty party = (PlayerParty) PartyManager.getPartyByMember(Characters.getPlayerCharacter(p).getUniqueCharacterId());
                if (party == null) {
                    MessageUtil.sendError(p, "You are not in a party.");
                    return;
                }


                for (Player pl : party.getOnlineMembers())
                    fm.send(pl);
            }
        });
    }

    @EventHandler
    public void onEntityDeath(CombatEngineDamageEvent event) {
        if (event.getAttacker() == null) return;

        if (!(event.getDamaged().getLivingEntity() instanceof Player
                && event.getAttacker().getLivingEntity() instanceof Player))
            return;

        if (!Characters.isPlayerCharacterLoaded((Player) event.getDamaged().getLivingEntity())) return;
        if (!Characters.isPlayerCharacterLoaded((Player) event.getAttacker().getLivingEntity())) return;

        PlayerCharacter pc1 = Characters.getPlayerCharacter((Player) event.getDamaged().getLivingEntity());
        PlayerCharacter pc2 = Characters.getPlayerCharacter((Player) event.getAttacker().getLivingEntity());

        IParty party = PartyManager.getPartyByMember(pc1.getUniqueCharacterId());
        if (party != null && party.getMembers().contains(pc2.getUniqueCharacterId()))
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
                    PartyCommands.cmdPartyInvite(p14, clickedPc.getPlayer());
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
                                PartyCommands.cmdPartyKick(p13, clickedPc.getPlayer());
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
                            PartyCommands.cmdPartyJoin(p12, clickedPc.getPlayer());
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