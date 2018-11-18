package com.legendsofvaleros.scheduler;

/*
 * Created by Crystall on 10/10/2018
 * Represents an executing thread for a module
 */
public class InternalTask {
    private InternalScheduler executor = null;
    private Runnable command;

    private boolean executed = false;
    private boolean repeat = false;
    private int delay = 0;
    private long net = 0;
    private long ri = 1;

    public InternalTask(Runnable command) {
        this.command = command;
    }

    public void setTask(Runnable command) {
        this.command = command;
    }

    public void setExecutor(InternalScheduler executor) {
        this.executor = executor;
    }

    public InternalScheduler getExecutor() {
        return executor;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public boolean wasExecuted() {
        return executed;
    }

    public void setRepeating(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean getRepeating() {
        return repeat;
    }

    public void run() {
        if(command == null)
            throw new RuntimeException("Attempted to run() an empty InternalTask.");
        command.run();
    }

    public void cancel() {
        setRepeating(false);
    }

    public void setNextExecuteTick(long l) {
        net = l;
    }

    public long nextExecuteTick() {
        return net;
    }

    public long getRepeatingInterval() {
        return ri;
    }

    public void setRepeatingInterval(long i) {
        ri = i;
    }
}
