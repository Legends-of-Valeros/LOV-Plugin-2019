package com.legendsofvaleros.modules.friends.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.friends.FriendRequest;
import com.legendsofvaleros.modules.friends.FriendsController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Crystall on 07/12/2019
 */
@CommandAlias("friends|f")
public class FriendCommands extends BaseCommand {

    @Subcommand("add")
    @Description("Sends a friend request do another player.")
    public void cmdAdd(Player sender, OnlinePlayer player) {
        Player target = player.getPlayer();
        if (!Characters.isPlayerCharacterLoaded(sender)) {
            MessageUtil.sendError(sender, target.getDisplayName() + " is not online.");
            return;
        }
        if (player.getPlayer().equals(sender)) {
            MessageUtil.sendError(sender, "You can't send yourself a friend request!");
            return;
        }
        if (FriendsController.getInstance().areFriends(sender, target)) {
            MessageUtil.sendError(sender, "You are already friends with " + ChatColor.UNDERLINE + target.getDisplayName());
            return;
        }
        if (FriendsController.hasPendingRequest(target)) {
            MessageUtil.sendError(sender, ChatColor.UNDERLINE + target.getDisplayName() + ChatColor.RESET + ChatColor.RED + " has a pending friend request.");
            return;
        }

        MessageUtil.sendInfo(sender, "Friend request sent to " + ChatColor.UNDERLINE + target.getDisplayName());
        MessageUtil.sendInfo(target, "You received a friend request from " + ChatColor.UNDERLINE + sender.getDisplayName());
        FriendsController.getPending().add(new FriendRequest(sender, target));
    }

    @Subcommand("remove")
    @Description("Removes a friend.")
    public void cmdRemove(Player sender, OfflinePlayer offlinePlayer) {
        UUID target = offlinePlayer.getUniqueId();
        if (!FriendsController.getInstance().areFriends(sender.getUniqueId(), target)) {
            MessageUtil.sendError(sender, "You are not befriend with " + ChatColor.UNDERLINE + offlinePlayer.getName());
            return;
        }

        FriendsController.getInstance().deleteFriend(sender.getUniqueId(), target).onSuccess(() -> {
            FriendsController.getInstance().getFriends(sender.getUniqueId()).remove(target);
            MessageUtil.sendError(sender, "You are no longer befriend with " + ChatColor.UNDERLINE + offlinePlayer.getName());
            if (offlinePlayer.getPlayer() != null) {
                MessageUtil.sendError(offlinePlayer.getPlayer(), "You are no longer befriend with " + ChatColor.UNDERLINE + sender.getDisplayName());
            }

            FriendsController.getInstance().getFriends(sender.getUniqueId()).remove(target);
            FriendsController.getInstance().getFriends(target).remove(sender.getUniqueId());
        }).onFailure(Throwable::printStackTrace);
    }

    @Subcommand("accept")
    @Description("Accepts a pending friend request")
    public void cmdAccept(Player sender) {
        if (!Characters.isPlayerCharacterLoaded(sender.getUniqueId())) {
            return;
        }
        if (!FriendsController.hasPendingRequest(sender)) {
            MessageUtil.sendError(sender, "You don't have any pending friend requests.");
            return;
        }

        FriendsController.getRequest(sender).success();
    }

    @Subcommand("deny")
    @Description("Denies a pending friend request")
    public void cmdDeny(Player sender) {
        if (!Characters.isPlayerCharacterLoaded((sender).getUniqueId())) {
            return;
        }
        if (!FriendsController.hasPendingRequest(sender)) {
            MessageUtil.sendError(sender, "You don't have any pending friend requests.");
            return;
        }

        FriendsController.getRequest(sender).deny();
    }

}

