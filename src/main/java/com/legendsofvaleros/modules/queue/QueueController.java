package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.modules.arena.Arena;
import com.legendsofvaleros.modules.queue.commands.QueueCommands;
import com.legendsofvaleros.util.MessageUtil;
import com.legendsofvaleros.util.commands.TemporaryCommand;
import org.bukkit.entity.Player;

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

    public boolean joinQueue(Class clazz, Player player) {
        // TODO
        return true;
    }

    public boolean joinQueue(Queue queue, Player player) {
        // TODO
        return true;
    }

    public boolean joinQueue(String queueName, Player player) {
        // TODO
        return true;
    }

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

    /**
     * Returns a player's queue, null otherwise
     * @param player
     * @return
     */
    public Queue getPlayerQueue(Player player) {
        return queues.stream().filter(q -> q.getQueuedPlayers().contains(player)).findAny().orElse(null);
    }


}
