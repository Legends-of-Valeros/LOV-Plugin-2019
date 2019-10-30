package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.module.annotation.ModuleInfo;
import com.legendsofvaleros.modules.queue.commands.QueueCommands;
import com.legendsofvaleros.modules.queue.events.QueueAcceptEvent;
import com.legendsofvaleros.modules.queue.events.QueueDenyEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by Crystall on 08/02/2019
 */
@ModuleInfo(name = "Queue", info = "")
public class QueueController extends ListenerModule {
    private static QueueController instance;

    private ArrayList<Queue> queues = new ArrayList<>();

    public static QueueController getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        LegendsOfValeros.getInstance().getCommandManager().registerCommand(new QueueCommands());
    }

    public <T> boolean registerQueue(Class<T> clazz, int minPlayer) {
        Queue<T> queue = new Queue<>((T) clazz, minPlayer);
        if (queues.contains(queue)) {
            MessageUtil.sendException(this, "Could not add queue " + queue.getQueueName() + ". Queue already exists!");
            return false;
        }
        if (queues.add(queue)) {
            getLogger().log(Level.INFO, "Registered queue: {0}.", queue.getQueueName());
            return true;
        }

        getLogger().log(Level.WARNING, "Failed to register queue: {0}.", queue.getQueueName());
        return false;
    }

    /**
     * Lets a player join a queue if he is not already in the queue
     * @param clazz
     * @param player
     * @return
     */
    public boolean joinQueue(Class clazz, Player player) {
        Queue queue = getQueueByType(clazz);
        if (queue == null) {
            MessageUtil.sendError(player, "Unable to find queue " + ChatColor.WHITE + clazz.getSimpleName());
            return false;
        }

        if (isInQueue(player)) {
            MessageUtil.sendError(player, "You are already in the queue " + ChatColor.WHITE + queue.getQueueName());
            return false;
        }

        if (queue.addPlayerToQueue(player)) {
            MessageUtil.sendInfo(player, "You have successfully joined the queue " + ChatColor.WHITE + queue.getQueueName());
            MessageUtil.sendInfo(player, "Currently queued players: " + ChatColor.WHITE + queue.getQueuedPlayers().size());
            return true;
        }

        MessageUtil.sendError(player, "Failed to add you to the queue " + ChatColor.WHITE + queue.getQueueName());
        return false;
    }

    /**
     * Lets a player leave the queue if he is part of it
     * @param player
     * @return if successful
     */
    public boolean leaveQueue(Player player) {
        Queue queue = getPlayerQueue(player);
        if (queue == null) {
            MessageUtil.sendError(player, "You are not in a queue!");
            return false;
        }

        if (queue.removePlayerFromQueue(player)) {
            MessageUtil.sendInfo(player, "You successfully left the queue " + ChatColor.WHITE + queue.getQueueName());
            return true;
        }

        return false;
    }

    /**
     * Returns if a player is in the given queue
     * @param player
     * @param queueName
     * @return
     */
    public boolean isInSpecificQueue(Player player, String queueName) {
        return queues.stream().filter(q -> q.getQueuedPlayers().contains(player)).filter(queue1 -> queue1.getQueueName().equals(queueName)).findAny().orElse(null) != null;
    }

    public boolean isInSpecificQueue(Player player, Class clazz) {
        return getPlayerQueue(player).get() == clazz;
    }

    public boolean isInQueue(Player player) {
        return queues.stream().anyMatch(q -> q.getQueuedPlayers().contains(player));
    }

    /**
     * Returns a player's queue, null otherwise
     * @param player
     * @return
     */
    public Queue getPlayerQueue(Player player) {
        return queues.stream().filter(q -> q.getQueuedPlayers().contains(player)).findAny().orElse(null);
    }

    public Queue getQueueByType(Class clazz) {
        return queues.stream().filter(q -> q.get().equals(clazz)).findAny().orElse(null);
    }

    /**
     * Shows the given player the queue scoreboard gui
     * @param player
     */
    public void showQueueGui(Player player) {
        //TODO
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerAccept(QueueAcceptEvent event) {

    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerDeny(QueueDenyEvent event) {

    }

}
