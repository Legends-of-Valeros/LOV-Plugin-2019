package com.legendsofvaleros.scheduler;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InternalScheduler extends Thread {
    private long[] last_ticks = new long[]{1, 1, 1, 1, 1};
    private double tps = 20;
    private long tick_duration = 0;
    private ConcurrentLinkedQueue<InternalTask> list = new ConcurrentLinkedQueue<>();
    private String name;
    private long tick = 0;
    private int prev_task_amount = 0;

    public InternalScheduler(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        this.setName(name);
        while (!LegendsOfValeros.shutdown) {
            tick++;
            InternalTask curr;
            LinkedList<InternalTask> requeue = new LinkedList<InternalTask>();
            prev_task_amount = 0;
            while (!list.isEmpty()) {
                curr = list.poll();
                prev_task_amount++;
                if (curr.nextExecuteTick() == tick) {
                    try {
                        curr.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    curr.setExecuted(true);
                    if (curr.getRepeating()) {
                        requeue.add(curr);
                        curr.setNextExecuteTick(tick + curr.getRepeatingInterval());
                    }
                } else {
                    if (curr.nextExecuteTick() > tick) requeue.add(curr);
                }
            }

            for (InternalTask r : requeue) list.add(r);

            tps = 1000D / ((System.currentTimeMillis() - last_ticks[0] + 0D) / 5D);
            tps = (Math.round(tps * 10) + 0D) / 10D;
            last_ticks[0] = last_ticks[1];
            last_ticks[1] = last_ticks[2];
            last_ticks[2] = last_ticks[3];
            last_ticks[3] = last_ticks[4];
            last_ticks[4] = System.currentTimeMillis();

            long still_wait = (long) ((50 - (System.currentTimeMillis() - last_ticks[3])) * 2.5);
            tick_duration = System.currentTimeMillis() - last_ticks[3];

            if (still_wait > 0) {
                try {
                    Thread.sleep(still_wait);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void async(Runnable command) {
        executeInMyCircle(command);
    }

    public void sync(Runnable command) {
        executeInSpigotCircle(command);
    }

    public void executeInMyCircle(Runnable task) { executeInMyCircle(new InternalTask(task)); }
    public void executeInMyCircle(InternalTask task) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + 1);
        list.add(task);
    }

    public InternalTask executeInMyCircleLater(Runnable task, long delay) {
        InternalTask it;
        executeInMyCircleLater(it = new InternalTask(task), delay);
        return it;
    }
    public void executeInMyCircleLater(InternalTask task, long delay) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + (delay <= 0 ? 1 : delay));
        list.add(task);
    }

    public InternalTask executeInMyCircleTimer(Runnable task, long delay, long interval) {
        InternalTask it;
        executeInMyCircleTimer(it = new InternalTask(task), delay, interval);
        return it;
    }
    public void executeInMyCircleTimer(InternalTask task, long delay, long interval) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + (delay <= 0 ? 1 : delay));
        task.setRepeating(true);
        task.setRepeatingInterval(interval);
        list.add(task);
    }

    public void executeInSpigotCircle(Runnable task) { executeInSpigotCircle(new InternalTask(task)); }
    public void executeInSpigotCircle(final InternalTask task) {
        task.setExecutor(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                task.setExecuted(true);
            }
        }.runTask(LegendsOfValeros.getInstance());
    }

    public void executeInSpigotCircleLater(Runnable task, long delay) { executeInSpigotCircleLater(new InternalTask(task), delay); }
    public void executeInSpigotCircleLater(final InternalTask task, long delay) {
        task.setExecutor(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                task.setExecuted(true);
            }
        }.runTaskLater(LegendsOfValeros.getInstance(), delay);
    }

    public void executeInSpigotCircleTimer(Runnable task, long delay, long interval) { executeInSpigotCircleTimer(new InternalTask(task), delay, interval); }
    public void executeInSpigotCircleTimer(final InternalTask task, long delay, long interval) {
        task.setExecutor(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                task.setExecuted(true);
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), delay, interval);
    }

    public double getTPS() {
        return tps;
    }

    public double getTickDuration() {
        return tick_duration;
    }

    public InternalScheduler startup() {
        if (!this.isAlive()) start();
        return this;
    }

    public int getPrevTaskAmount() {
        return prev_task_amount;
    }
}
