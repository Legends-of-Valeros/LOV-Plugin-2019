package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.queue.commands.QueueCommands;
import com.legendsofvaleros.modules.queue.events.QueueAcceptEvent;
import com.legendsofvaleros.modules.queue.events.QueueDenyEvent;
import com.legendsofvaleros.modules.queue.events.QueueReadyEvent;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;

/**
 * Created by Crystall on 08/02/2019
 */
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

    @Override
    public void onUnload() {
        super.onUnload();
    }

    public <T> boolean registerQueue(Class<T> cl, int minPlayer) {
        Queue queue = new Queue<T>(minPlayer);
        if (queues.contains(queue)) {
            MessageUtil.sendException(this, "Could not add queue " + queue.getQueueName() + ". Queue already exists!");
            return false;
        }
        return queues.add(queue);
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
            return false;
        }
        if (isInQueue(player, queue.getQueueName())) {
            return false;
        }

        if (queue.addPlayerToQueue(player)) {

            return true;
        }

        return false;
    }

    /**
     * Lets a player leave the queue if he is part of it
     * @param clazz
     * @param player
     * @return
     */
    public boolean leaveQueue(Class clazz, Player player) {
        // TODO
        return true;
    }

    public boolean leaveQueue(Queue queue, Player player) {
        // TODO
        return true;
    }

    public boolean leaveQueue(String queueName, Player player) {
        // TODO
        return true;
    }

    /**
     * Returns if a player is in the given queue
     * @param player
     * @param queueName
     * @return
     */
    public boolean isInQueue(Player player, String queueName) {
        return queues.stream().filter(q -> q.getQueuedPlayers().contains(player)).filter(queue1 -> queue1.getQueueName().equals(queueName)).findAny().orElse(null) != null;
    }

    public boolean isInQueue(Player player, Class clazz) {
        return getPlayerQueue(player).get() == clazz;
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

    /**
     * @param event
     */
    @EventHandler
    public void onQueueReadyEvent(QueueReadyEvent event) {

    }


}
