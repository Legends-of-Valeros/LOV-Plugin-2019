package com.legendsofvaleros.modules.friends;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterFinishLoadingEvent;
import com.legendsofvaleros.modules.characters.events.PlayerCharacterLogoutEvent;
import com.legendsofvaleros.modules.friends.commands.FriendCommands;
import com.legendsofvaleros.modules.gear.commands.ItemCommands;
import com.legendsofvaleros.scheduler.InternalTask;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Crystall on 07/12/2019
 */
public class FriendsController extends FriendsAPI {

    private static List<FriendRequest> pending = new ArrayList<>();
    private static FriendsController instance;

    public static FriendsController getInstance() {
        if (instance == null) {
            instance = new FriendsController();
        }
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new FriendCommands());

        // Expire friend requests.
        getScheduler().executeInMyCircleTimer(new InternalTask(() -> {
            new ArrayList<>(pending).forEach(FriendRequest::tryExpire);
        }), 0L, 100L);
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    private void sendFriendStatus(Player p, String state) {
        String message = ChatColor.UNDERLINE + p.getDisplayName() + ChatColor.AQUA + " has " + state;
        for (UUID uuid : playerFriendsMap.get(p.getUniqueId())) {
            if (Characters.isPlayerCharacterLoaded(uuid)) {
                PlayerCharacter friendPC = Characters.getPlayerCharacter(uuid);
                MessageUtil.sendInfo(friendPC.getPlayer(), message);
            }
        }
    }

    /**
     * Get a friend request between two players.
     * @param sender   - The request sender. If null, only the receiver will be checked.
     * @param receiver - The request receiver. If null, only the sender will be checked.
     * @return request - Null if not found.
     */
    public static FriendRequest getRequest(Player sender, Player receiver) {
        return getPending().stream().filter(r -> sender == null || r.getSender().equals(sender))
                .filter(r -> receiver == null || r.getReceiver().equals(receiver)).findAny().orElse(null);
    }

    /**
     * Get a friend request that a given player is a part of.
     * @param player The player to check requests for.
     * @return request
     */
    public static FriendRequest getRequest(Player player) {
        return getPending().stream().filter(r -> r.getSender().equals(player) || r.getReceiver().equals(player)).findAny().orElse(null);
    }

    /**
     * Checks if the given player has a pending friend request
     * @param player
     * @return
     */
    public static boolean hasPendingRequest(Player player) {
        return getPending().stream().filter(r -> r.getReceiver().equals(player)).findAny().orElse(null) != null;
    }

    /**
     * Checks if two online players are friends.
     * @param p1 - Player 1
     * @param p2 - Player 2
     * @return areFriends
     */
    public boolean areFriends(Player p1, Player p2) {
        return playerFriendsMap.get(p1.getUniqueId()).contains(p2.getUniqueId()) || playerFriendsMap.get(p2.getUniqueId()).contains(p1.getUniqueId());
    }

    @EventHandler
    public void onCharacterLoad(PlayerCharacterFinishLoadingEvent event) {
        sendFriendStatus(event.getPlayer(), "joined");
    }

    @EventHandler
    public void onQuit(PlayerCharacterLogoutEvent event) {
        Player p = event.getPlayer();
        sendFriendStatus(p, "left");

        FriendRequest request = getRequest(p);
        if (request != null) {
            // Player disconnected, cancel request.
            request.cancel(p);
        }

        // Remove the friends for this player from the memory
        playerFriendsMap.remove(p.getUniqueId());
    }

    public static List<FriendRequest> getPending() {
        return pending;
    }

    public static void setPending(List<FriendRequest> pending) {
        FriendsController.pending = pending;
    }
}
