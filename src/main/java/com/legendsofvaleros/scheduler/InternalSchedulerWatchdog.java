package com.legendsofvaleros.scheduler;

import com.legendsofvaleros.LegendsOfValeros;

public class InternalSchedulerWatchdog extends Thread {
    private InternalScheduler scheduler;

    public InternalSchedulerWatchdog(InternalScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        while(!scheduler.isShuttingDown() || scheduler.getTasksRemaining() > 0) {
            // Ignore schedulers that haven't ticked at all, yet
            if(scheduler.getLastTickTime() != 0) {
                // If the last tick was more than ten seconds ago,
                // warn that the scheduler may be deadlocked.
                if (System.currentTimeMillis() - scheduler.getLastTickTime() > 10000L)
                    LegendsOfValeros.getInstance().getLogger().warning("'" + scheduler.getName() + "' hasn't ticked in a long time! Is it deadlocked?");
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public InternalSchedulerWatchdog startup() {
        if (!this.isAlive())
            start();
        return this;
    }
}
