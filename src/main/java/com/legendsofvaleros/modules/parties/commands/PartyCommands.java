package com.legendsofvaleros.modules.parties.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.legendsofvaleros.modules.characters.api.CharacterId;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.parties.PartyManager;
import com.legendsofvaleros.modules.parties.core.PlayerParty;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("party|p|lov party")
public class PartyCommands extends BaseCommand {
    @Subcommand("create")
    @Description("Create a new party.")
    public static void cmdPartyCreate(Player player) {
        PlayerCharacter pc = getPlayerCharacter(player);
        if (getCurrentParty(pc) != null) {
            MessageUtil.sendError(player, "You are already in a party. If you'd like to leave, use /party leave");
            return;
        }

        PartyManager.addMember(new PlayerParty(), pc.getUniqueCharacterId());
    }

    @Subcommand("join")
    @Description("Join an existing party that you have been invited to.")
    public static void cmdPartyJoin(Player player, OnlinePlayer join) {
        PlayerCharacter pc = getPlayerCharacter(player);
        if (getCurrentParty(pc) != null) {
            MessageUtil.sendError(player, "You are already in a party. If you'd like to leave, use /party leave");
            return;
        }

        PlayerCharacter tpc = getPlayerCharacter(join.getPlayer());
        PlayerParty pp = getCurrentParty(tpc);
        if (pp == null) {
            MessageUtil.sendError(player, "That player is not in a party.");
        } else if (!pp.invitations.contains(pc.getUniqueCharacterId())) {
            MessageUtil.sendError(player, "You have not been invited to that party.");
        } else {
            PartyManager.addMember(pp, pc.getUniqueCharacterId());
        }
    }

    @Subcommand("leave")
    @Description("Leave your party.")
    public void cmdPartyLeave(Player player) {
        PlayerCharacter pc = getPlayerCharacter(player);
        if (getCurrentParty(pc) == null) {
            MessageUtil.sendError(player, "You are not in a party. If you'd like to create one, use /party create");
            return;
        }

        PartyManager.removeMember(getCurrentParty(pc), pc.getUniqueCharacterId());
    }

    @Subcommand("invite")
    @Description("Invite a player to your party.")
    public static void cmdPartyInvite(Player player, OnlinePlayer invite) {
        PlayerCharacter pc = getPlayerCharacter(player);
        if (getCurrentParty(pc) == null) {
            PartyManager.addMember(new PlayerParty(), pc.getUniqueCharacterId());
        }

        PlayerCharacter tpc = getPlayerCharacter(invite.getPlayer());
        PlayerParty pp = getCurrentParty(pc);
        if (!pp.invitations.contains(tpc.getUniqueCharacterId())) {
            pp.invitations.add(tpc.getUniqueCharacterId());
            MessageUtil.sendUpdate(player, "Invited " + tpc.getPlayer().getName() + " to the party.");
            MessageUtil.sendInfo(tpc.getPlayer(), player.getDisplayName() + " has invited you to their party.");
        } else {
            MessageUtil.sendError(player, "You have already invited that player.");
        }
    }

    @Subcommand("kick")
    @Description("Kick a player from your party.")
    public static void cmdPartyKick(Player player, OfflinePlayer kick) {
        PlayerCharacter pc = getPlayerCharacter(player);
        if (getCurrentParty(pc) == null) {
            MessageUtil.sendError(player, "You are not in a party. If you'd like to create one, use /party create");
            return;
        }

        PlayerParty pp = getCurrentParty(pc);
        if (pp.getLeader().getPlayerId().equals(pc.getPlayer().getUniqueId())) {
            CharacterId targetId = null;
            for (CharacterId id : pp.getMembers())
                if (id.getPlayerId().equals(kick.getUniqueId())) {
                    targetId = id;
                    break;
                }

            if (targetId == null) {
                MessageUtil.sendError(player, "That player is not in the party.");
                return;
            }

            MessageUtil.sendUpdate(player, player.getDisplayName() + " has kicked " + Bukkit.getOfflinePlayer(targetId.getPlayerId()).getName() + " from the party.");
            PartyManager.removeMember(pp, targetId);
        } else {
            MessageUtil.sendError(player, "You are not the party leader.");
            return;
        }
    }

    public static PlayerCharacter getPlayerCharacter(CommandSender sender) {
        if (!Characters.isPlayerCharacterLoaded((Player) sender)) return null;
        return Characters.getPlayerCharacter((Player) sender);
    }

    public static PlayerParty getCurrentParty(PlayerCharacter pc) {
        return (PlayerParty) PartyManager.getPartyByMember(pc.getUniqueCharacterId());
    }

    @Default
    @HelpCommand
    public void cmdHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}