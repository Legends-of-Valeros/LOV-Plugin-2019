package com.legendsofvaleros.modules.friends.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.friends.FriendRequest;
import com.legendsofvaleros.modules.friends.FriendsController;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 07/12/2019
 */
@CommandAlias("friends")
public class FriendCommands extends BaseCommand {

    @Subcommand("add")
    @Description("Sends a friend request do another player.")
    public void cmdAdd(CommandSender sender, Player target) {
        if (sender instanceof ConsoleCommandSender) {
            MessageUtil.sendError(sender, "You can't execute this command in the console");
            return;
        }
        if (!Characters.isPlayerCharacterLoaded((Player) sender)) {
            return;
        }
        if (Characters.isPlayerCharacterLoaded(target)) {
            MessageUtil.sendError(sender, target.getDisplayName() + " is not online.");
            return;
        }
        if (FriendsController.getInstance().areFriends(((Player) sender), target)) {
            MessageUtil.sendError(sender, "You are already friends with " + ChatColor.UNDERLINE + target.getDisplayName());
            return;
        }
        if (FriendsController.hasPendingRequest(target)) {
            MessageUtil.sendError(sender, ChatColor.UNDERLINE + target.getDisplayName() + ChatColor.RESET + ChatColor.RED + " has a pending friend request.");
            return;
        }

        MessageUtil.sendInfo(sender, "Friend request sent to " + ChatColor.UNDERLINE + target.getDisplayName());
        MessageUtil.sendInfo(target, "You received a friend request from " + ChatColor.UNDERLINE + target.getDisplayName());
        FriendsController.getPending().add(new FriendRequest((Player) sender, target));
    }

    @Subcommand("remove")
    @Description("Removes a friend.")
    public void cmdRemove(CommandSender sender, Player target) {
        if (sender instanceof ConsoleCommandSender) {
            MessageUtil.sendError(sender, "You can't execute this command in the console");
            return;
        }
        if (!FriendsController.getInstance().areFriends((Player) sender, target)) {
            MessageUtil.sendError(sender, "You are not befriend with " + ChatColor.UNDERLINE + target.getDisplayName());
            return;
        }

        FriendsController.getInstance().deleteFriend(((Player) sender).getUniqueId(), target.getUniqueId()).onSuccess(() -> {

        }).onFailure(Throwable::printStackTrace);
    }

    @Subcommand("accept")
    @Description("Accepts a pending friend request")
    public void cmdAccept(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            MessageUtil.sendError(sender, "You can't execute this command in the console!");
            return;
        }
        if (!Characters.isPlayerCharacterLoaded(((Player) sender).getUniqueId())) {
            return;
        }
        if (FriendsController.hasPendingRequest((Player) sender)) {
            MessageUtil.sendError(sender, "You don't have any pending friend requests.");
            return;
        }

        FriendsController.getRequest((Player) sender).success();
    }

    @Subcommand("deny")
    @Description("Denies a pending friend request")
    public void cmdDeny(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            MessageUtil.sendError(sender, "You can't execute this command in the console");
            return;
        }
        if (!Characters.isPlayerCharacterLoaded(((Player) sender).getUniqueId())) {
            return;
        }
        if (FriendsController.hasPendingRequest((Player) sender)) {
            MessageUtil.sendError(sender, "You don't have any pending friend requests.");
            return;
        }

        FriendsController.getRequest((Player) sender).deny();
    }

}

