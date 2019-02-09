package com.legendsofvaleros.scheduler;

import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InternalScheduler extends Thread {
    private static final long TICK_TIME = 1000L / 20L;

    // Delayed timings to dump on shutdown. (Destroy tasks delayed 5 seconds)
    // XXX: A task that requeues itself after running with a delay of less
    //      than five seconds each time will keep the server locked. Make
    //      sure to always use repeating tasks in this situation so this is
    //      never a problem
    private static final int SHUTDOWN_DUMP = 20 * 5;
    private static final int SHUTDOWN_NOTIFY = 20 * 5;

    // Keep five seconds of timings
    private static final int TIMINGS_COUNT = 20 * 5;

    private static Map<String, InternalScheduler> all = new HashMap<>();
    public static Collection<InternalScheduler> getAllSchedulers() { return all.values(); }

    private String name;

    private boolean shutdown = false;
    public void shutdown() { this.shutdown = true; }
    public boolean isShuttingDown() { return shutdown; }

    private Queue<InternalTask> list = new ConcurrentLinkedQueue<>();
    public int getTasksRemaining() { return list.size(); }
    public InternalTask[] getTasksQueued() { return list.toArray(new InternalTask[0]); }

    int totalS = 0, totalA = 0;
    public int getSyncTasksFired() { return totalS; }
    public int getAsyncTasksFired() { return totalA; }

    private long totalBehind = 0;
    public long getTotalBehind() { return totalBehind; }

    private long tick = 0;
    public long getCurrentTick() { return tick; }

    private long lastTickTime = 0L;
    public long getLastTickTime() { return lastTickTime; }

    public List<Long> timings = new ArrayList<>();

    private final InternalSchedulerWatchdog watchdog;

    public InternalScheduler(String name) {
        this.name = name;
        this.watchdog = new InternalSchedulerWatchdog(this);
    }

    @Override
    public void run() {
        if(all.containsKey(name))
            throw new IllegalStateException("A scheduler with that name is already running!");
        all.put(name, this);

        this.setName(name);

        try {
            long lastTime;
            InternalTask curr;
            List<InternalTask> fired = new LinkedList<>();
            List<InternalTask> requeue = new LinkedList<>();

            while (!shutdown || list.size() > 0) {
                // Notify once a second of remaining tasks
                if (shutdown && tick % SHUTDOWN_NOTIFY == 0) {
                    LegendsOfValeros.getInstance().getLogger().warning("'" + name + "' is waiting for " + list.size() + " tasks to complete...");
                    for(InternalTask task : list) {
                        if(!task.getName().contains("com.legendsofvaleros"))
                            LegendsOfValeros.getInstance().getLogger().warning("  - " + task.getName());
                    }
                }

                lastTime = System.currentTimeMillis();
                requeue.clear();
                fired.clear();

                tick++;
                lastTickTime = System.currentTimeMillis();

                while (!list.isEmpty()) {
                    curr = list.poll();

                    if (shutdown) {
                        // Ignore repeating tasks on shutdown
                        if (curr.getRepeating())
                            continue;

                        // Ignore long delayed tasks on shutdown
                        if (tick - curr.nextExecuteTick() > SHUTDOWN_DUMP)
                            continue;
                    }

                    if (curr.nextExecuteTick() == tick) {
                        try {
                            fired.add(curr);
                            curr.run();
                        } catch (Exception e) {
                            MessageUtil.sendSevereException(name, e);
                        }

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
                while (timings.size() > TIMINGS_COUNT)
                    timings.remove(0);

                long timeToSync = TICK_TIME - timeTaken;

                if (timeToSync > 0) {
                    try {
                        Thread.sleep(timeToSync);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    totalBehind += Math.abs(timeToSync);

                    // If we fall behind, log the creation traces of all fired tasks. The slowdown may
                    // be the result of queueing many tasks, in which case this can be ignored, but this can
                    // assist in tracking down ornery tasks.
                    LegendsOfValeros.getInstance().getLogger().warning("Scheduler '" + name + "' fell behind by " + Math.abs(timeTaken) + "ms!");
                    if (fired.size() > 0) {
                        LegendsOfValeros.getInstance().getLogger().warning("----------------------------------------");
                        for (InternalTask task : fired) {
                            int i = -1;
                            for(String line : task.getTrace().split("\n")) {
                                if(i == -1) {
                                    // Ignore non-LOV packages
                                    if (!line.contains("com.legendsofvaleros")) continue;
                                    // Ignore scheduler package
                                    if (line.contains("com.legendsofvaleros.scheduler")) continue;

                                    LegendsOfValeros.getInstance().getLogger().warning(task.getName());
                                }

                                i++;

                                LegendsOfValeros.getInstance().getLogger().warning(line);

                                // Don't print too many lines. After an amount, it's just spam.
                                if(i > 6 && !line.contains("legendsofvaleros")) break;
                            }
                        }
                        LegendsOfValeros.getInstance().getLogger().warning("----------------------------------------");
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        shutdown = true;
        all.remove(name);
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
        if(!isAlive() && shutdown)
            throw new IllegalStateException("Cannot add a task to a scheduler that has shut down!");

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
        if(!isAlive() && shutdown)
            throw new IllegalStateException("Cannot add a task to a scheduler that has shut down!");

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
        if(!isAlive() && shutdown)
            throw new IllegalStateException("Cannot add a task to a scheduler that has shut down!");

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
        if(!isAlive() && shutdown)
            throw new IllegalStateException("Cannot add a task to a scheduler that has shut down!");

        task.setExecutor(this);
        task.setSync(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTask(LegendsOfValeros.getInstance());
    }

    public InternalTask executeInSpigotCircleLater(Runnable task, long delay) {
        InternalTask it;
        executeInSpigotCircleLater(it = new InternalTask(task), delay);
        return it;
    }
    public void executeInSpigotCircleLater(final InternalTask task, long delay) {
        if(!isAlive() && shutdown)
            throw new IllegalStateException("Cannot add a task to a scheduler that has shut down!");

        task.setExecutor(this);
        task.setSync(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskLater(LegendsOfValeros.getInstance(), delay);
    }

    public InternalTask executeInSpigotCircleTimer(Runnable task, long delay, long interval) {
        InternalTask it;
        executeInSpigotCircleTimer(it = new InternalTask(task), delay, interval);
        return it;
    }
    public void executeInSpigotCircleTimer(final InternalTask task, long delay, long interval) {
        if(!isAlive() && shutdown)
            throw new IllegalStateException("Cannot add a task to a scheduler that has shut down!");

        task.setExecutor(this);
        task.setSync(true);
        task.setRepeating(true);
        task.setRepeatingInterval(interval);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (IllegalStateException e) {
                    cancel();
                }
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

    public InternalScheduler startup() {
        if (!this.isAlive()) {
            start();
            this.watchdog.startup();
        }

        return this;
    }
}
