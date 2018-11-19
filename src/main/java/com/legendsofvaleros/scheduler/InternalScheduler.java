package com.legendsofvaleros.scheduler;

import com.legendsofvaleros.LegendsOfValeros;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InternalScheduler extends Thread {
    private static final long TICK_TIME = 1000L / 20L;

    // Keep five seconds of timings
    private static final int TIMINGS_COUNT = 20 * 5;

    private String name;
    private Queue<InternalTask> list = new ConcurrentLinkedQueue<>();

    int totalS = 0, totalA = 0;
    public int getSyncTasksFired() { return totalS; }
    public int getAsyncTasksFired() { return totalA; }

    private long tick = 0;
    public List<Long> timings = new ArrayList<>();

    /*private long[] last_ticks = new long[] { 1, 1, 1, 1, 1 };
    private double tps = 20;
    private long tick_duration = 0;
    private long tick = 0;
    private int prev_task_amount = 0;*/

    public InternalScheduler(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        this.setName(name);

        long lastTime;

        while (!LegendsOfValeros.shutdown) {
            lastTime = System.currentTimeMillis();

            tick++;

            InternalTask curr;
            LinkedList<InternalTask> requeue = new LinkedList<>();

            // prev_task_amount = 0;

            while(!list.isEmpty()) {
                curr = list.poll();

                // prev_task_amount++;

                if(curr.nextExecuteTick() == tick) {
                    try {
                        curr.run();
                    } catch(Exception e) {
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

            long timeTaken = System.currentTimeMillis() - lastTime;

            timings.add(timeTaken);
            while(timings.size() > TIMINGS_COUNT)
                timings.remove(0);

            long timeToSync = TICK_TIME - timeTaken;

            /*tps = 1000D / ((System.currentTimeMillis() - last_ticks[0] + 0D) / 5D);
            tps = (Math.round(tps * 10) + 0D) / 10D;
            last_ticks[0] = last_ticks[1];
            last_ticks[1] = last_ticks[2];
            last_ticks[2] = last_ticks[3];
            last_ticks[3] = last_ticks[4];
            last_ticks[4] = System.currentTimeMillis();

            long still_wait = (long) ((50 - (System.currentTimeMillis() - last_ticks[3])) * 2.5);
            tick_duration = System.currentTimeMillis() - last_ticks[3];*/

            if (timeToSync > 0) {
                try {
                    Thread.sleep(timeToSync);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                LegendsOfValeros.getInstance().getLogger().warning("Scheduler '" + name + "' fell behind by " + Math.abs(timeTaken) + "ms!");
            }
        }
    }

    public void async(Runnable command) {
        executeInMyCircle(command);
    }

    public void sync(Runnable command) {
        executeInSpigotCircle(command);
    }

    public InternalTask executeInMyCircle(Runnable task) {
        InternalTask it;
        executeInMyCircle(it = new InternalTask(task));
        return it;
    }
    public void executeInMyCircle(InternalTask task) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + 1);
        task.setSync(false);
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
        task.setSync(false);
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
        task.setSync(false);
        list.add(task);
    }

    public InternalTask executeInSpigotCircle(Runnable task) {
        InternalTask it;
        executeInSpigotCircle(it = new InternalTask(task));
        return it;
    }
    public void executeInSpigotCircle(final InternalTask task) {
        task.setExecutor(this);
        task.setSync(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                task.setExecuted(true);
            }
        }.runTask(LegendsOfValeros.getInstance());
    }

    public InternalTask executeInSpigotCircleLater(Runnable task, long delay) {
        InternalTask it;
        executeInSpigotCircleLater(it = new InternalTask(task), delay);
        return it;
    }
    public void executeInSpigotCircleLater(final InternalTask task, long delay) {
        task.setExecutor(this);
        task.setSync(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                task.setExecuted(true);
            }
        }.runTaskLater(LegendsOfValeros.getInstance(), delay);
    }

    public InternalTask executeInSpigotCircleTimer(Runnable task, long delay, long interval) {
        InternalTask it;
        executeInSpigotCircleTimer(it = new InternalTask(task), delay, interval);
        return it;
    }
    public void executeInSpigotCircleTimer(final InternalTask task, long delay, long interval) {
        task.setExecutor(this);
        task.setSync(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                task.setExecuted(true);
            }
        }.runTaskTimer(LegendsOfValeros.getInstance(), delay, interval);
    }

    public synchronized Long getAverageTiming() {
        if(timings.size() == 0) return null;

        long time = 0;
        for(Long timing : timings)
            time += timing;
        return time / timings.size();
    }

    public synchronized double getLastTiming() {
        if(timings.size() == 0) return 0;
        return timings.get(timings.size() - 1);
    }

    public double getAverageTPSUsage() {
        Long timing = getAverageTiming();
        if(timing == null) return 0;
        double avg = timing / (1000D / 20D);
        return (int)(avg * 10000) / 10000D;
    }

    public double getAverageTPS() {
        return 20D - getAverageTPSUsage();
    }

    /*public double getTPS() {
        return tps;
    }*/

    /*public double getTickDuration() {
        return tick_duration;
    }*/

    public InternalScheduler startup() {
        if (!this.isAlive()) start();
        return this;
    }

    /*public int getPrevTaskAmount() {
        return prev_task_amount;
    }*/
}
