package com.legendsofvaleros.modules.queue;

import com.legendsofvaleros.module.ListenerModule;
import com.legendsofvaleros.util.MessageUtil;
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
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    public <T> boolean registerQueue(Queue<T> queue) {
        if (queues.contains(queue)) {
            MessageUtil.sendException(this, "Could not add queue " + queue.getQueueName() + ". Queue already exists!");
            return false;
        }
        return queues.add(queue);
    }

    public boolean joinQueue(Class clazz) {
        // TODO
        return true;
    }

    public boolean joinQueue(Queue queue) {
        // TODO
        return true;
    }

    public boolean joinQueue(String queueName) {
        // TODO
        return true;
    }

    public boolean leaveQueue(Class clazz) {
        // TODO
        return true;
    }

    public boolean leaveQueue(Queue queue) {
        // TODO
        return true;
    }

    public boolean leaveQueue(String queueName) {
        // TODO
        return true;
    }

    public boolean isInQueue(Player player) {
        // TODO
        return true;
    }


}
