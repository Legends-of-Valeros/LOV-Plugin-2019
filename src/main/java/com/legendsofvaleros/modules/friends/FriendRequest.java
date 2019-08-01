package com.legendsofvaleros.modules.friends;

import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Crystall on 07/12/2019
 */
public class FriendRequest {
    private Player sender;
    private Player receiver;
    private long expiry;

    private static final int EXPIRE_TIME = 30;

    public FriendRequest(Player sender, Player receiver) {
        this(sender, receiver, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(EXPIRE_TIME));
    }

    public FriendRequest(Player sender, Player receiver, long expiry) {
        this.sender = sender;
        this.receiver = receiver;
        this.expiry = expiry;
    }

    public void cancel(Player left) {
        FriendsController.getPending().remove(this);
        MessageUtil.sendInfo(sender.equals(left) ? receiver : sender, ChatColor.AQUA + "Request cancelled, " + left.getName() + " disconnected.");
    }

    /**
     * Expire this request if it has not been accepted in the correct time-frame.
     */
    public void tryExpire() {
        if (expiry > System.currentTimeMillis()) {
            return; // Has not expired yet.
        }

        FriendsController.getPending().remove(this);
        MessageUtil.sendInfo(sender, "Your friend request has expired.");
        MessageUtil.sendInfo(receiver, sender.getDisplayName() + "'s friend request has expired.");
    }

    /**
     * Finishes the friend request and triggers the saving
     */
    public void success() {
        FriendsController.getInstance().saveFriend(this);
        FriendsController.getPending().remove(this);
        MessageUtil.sendInfo(sender, "You are now friends with " + ChatColor.UNDERLINE + receiver.getDisplayName() + ".");
        MessageUtil.sendInfo(receiver, "You are now friends with " + ChatColor.UNDERLINE + sender.getDisplayName() + ".");
    }

    /**
     * Denies the request and cleans up
     */
    public void deny() {
        FriendsController.getPending().remove(this);
        MessageUtil.sendInfo(receiver, "You denied the friend request of " + ChatColor.UNDERLINE + receiver.getDisplayName() + ".");
        MessageUtil.sendInfo(sender, ChatColor.UNDERLINE + sender.getDisplayName() + " denied your friend request.");
    }

    public UUID getSenderID() {
        return sender.getUniqueId();
    }

    public UUID getReceiverID() {
        return receiver.getUniqueId();
    }

    public Player getReceiver() {
        return receiver;
    }

    public Player getSender() {
        return sender;
    }

    public long getExpiry() {
        return expiry;
    }
}